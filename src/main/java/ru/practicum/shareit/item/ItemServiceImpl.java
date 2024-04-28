package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsAndCommentsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final ItemRequestRepository itemRequestRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, ItemMapper itemMapper,
                           UserRepository userRepository, ItemRequestRepository itemRequestRepository,
                           BookingRepository bookingRepository, BookingMapper bookingMapper,
                           CommentRepository commentRepository, CommentMapper commentMapper) {
        this.itemRepository = itemRepository;
        this.itemMapper = itemMapper;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.bookingMapper = bookingMapper;
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
        this.itemRequestRepository = itemRequestRepository;
    }

    @Override
    public List<ItemWithBookingsAndCommentsDto> getAll(int userId, Pageable page) {
        List<Item> items = itemRepository.findAllByOwnerId(userId, page);

        List<Integer> itemsId = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());
        List<Comment> comments = commentRepository.findAllByItemsId(itemsId);
        List<Booking> bookings = bookingRepository.findAllByItemsId(itemsId);
        Map<Integer, List<Comment>> commentsByItem = comments.stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));
        Map<Integer, List<Booking>> bookingsByItem = bookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));
        List<ItemWithBookingsAndCommentsDto> itemsDto = new ArrayList<>();
        for (Item item : items) {
            ItemWithBookingsAndCommentsDto itemWithBookingsAndCommentsDto = itemMapper.convertItemToBookingDto(item);
            int itemId = item.getId();

            List<Booking> itemBooking = bookingsByItem.get(itemId);
            if (itemBooking != null) {
                setBookingsToItemDto(itemWithBookingsAndCommentsDto, itemBooking);
            }

            List<Comment> itemComments = commentsByItem.get(itemId);
            if (itemComments != null) {
                setCommentsToItemDto(itemWithBookingsAndCommentsDto, itemComments);
            }
            itemsDto.add(itemWithBookingsAndCommentsDto);
        }
        return itemsDto;
    }

    @Override
    public ItemDto create(ItemDto itemDto, int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с id " + userId + " не существует"));
        Item newItem = itemMapper.convertItemDto(itemDto);Integer requestId = itemDto.getRequestId();
        if (requestId != null) {
            ItemRequest itemRequest = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запроса с id " + requestId + " не существует"));
            newItem.setRequest(itemRequest);
        }
        newItem.setOwner(user);
        return itemMapper.convertItem(itemRepository.save(newItem));
    }

    @Override
    public ItemDto update(ItemDto itemDto, int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с id " + userId + " не существует"));
        Item existentItem = itemRepository.findById(itemDto.getId())
                .orElseThrow(() -> new NotFoundException("Предмета с id " + itemDto.getId() + " не существует"));

        Item existentItemCopy = existentItem.toBuilder().build();
        Item newItem = itemMapper.convertItemDto(itemDto);
        if (!Objects.equals(existentItem.getOwner(), user)) {
            throw new NotFoundException("Попытка изменить вещь пользователем, не являющимся владельцом");
        }
        if (newItem.getName() != null) {
            existentItemCopy.setName(newItem.getName());
        }
        if (newItem.getDescription() != null) {
            existentItemCopy.setDescription(newItem.getDescription());
        }
        if (newItem.getAvailable() != null) {
            existentItemCopy.setAvailable(newItem.getAvailable());
        }

        return itemMapper.convertItem(itemRepository.save(existentItemCopy));
    }

    @Override
    public ItemWithBookingsAndCommentsDto get(int itemId, int userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмета с id " + itemId + " не существует"));
        ItemWithBookingsAndCommentsDto itemWithBookingsDto = itemMapper.convertItemToBookingDto(item);
        if (item.getOwner().getId() == userId) { // Выдаем бронирования только хозяину вещи
            List<Booking> bookings = bookingRepository.findAllByItemId(itemId);
            setBookingsToItemDto(itemWithBookingsDto, bookings);
        }
        Sort sort = Sort.by("created").descending();
        List<Comment> comments = commentRepository.findAllByItemId(itemId, sort);
        setCommentsToItemDto(itemWithBookingsDto, comments);
        return itemWithBookingsDto;
    }

    @Override
    public List<ItemDto> search(String text, Pageable page) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        List<Item> items = itemRepository.search(text, page);
        return itemMapper.convertListItem(items);
    }

    private void setBookingsToItemDto(ItemWithBookingsAndCommentsDto itemWithBookingsDto,
                                      List<Booking> itemBookings) {
        Optional<Booking> lastBooking = itemBookings.stream()
                .filter(e -> e.getStartDate().isBefore(LocalDateTime.now()) && e.getStatus().equals(BookingState.APPROVED))
                .max(Comparator.comparing(Booking::getStartDate));
        Optional<Booking> nextBooking = itemBookings.stream()
                .filter(e -> e.getStartDate().isAfter(LocalDateTime.now()) && e.getStatus().equals(BookingState.APPROVED))
                .min(Comparator.comparing(Booking::getStartDate));
        BookingShortDto lastBookingDto = lastBooking.map(bookingMapper::convertBookingToShortDto).orElse(null);
        itemWithBookingsDto.setLastBooking(lastBookingDto);
        BookingShortDto nextBookingDto = nextBooking.map(bookingMapper::convertBookingToShortDto).orElse(null);
        itemWithBookingsDto.setNextBooking(nextBookingDto);
    }

    private void setCommentsToItemDto(ItemWithBookingsAndCommentsDto itemWithBookingsDto,
                                      List<Comment> itemComments) {
        itemWithBookingsDto.setComments(itemComments.stream()
                .map(commentMapper::convertComment)
                .collect(Collectors.toList()));
    }
}
