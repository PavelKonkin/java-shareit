package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
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
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, ItemMapper itemMapper,
                           UserRepository userRepository, UserMapper userMapper,
                           BookingRepository bookingRepository, BookingMapper bookingMapper,
                           CommentRepository commentRepository, CommentMapper commentMapper) {
        this.itemRepository = itemRepository;
        this.itemMapper = itemMapper;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.bookingRepository = bookingRepository;
        this.bookingMapper = bookingMapper;
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
    }

    @Override
    public List<ItemWithBookingsAndCommentsDto> getAll(int userId) {
        List<Item> items = itemRepository.findAllByOwnerIdOrderById(userId);
        List<ItemWithBookingsAndCommentsDto> itemsDto = itemMapper.convertListItemToBookingDto(items);
        for (ItemWithBookingsAndCommentsDto itemDto : itemsDto) {
            setBookingsToItemDto(itemDto);
            setCommentsToItemDto(itemDto);
        }
        return itemsDto;
    }

    @Override
    public ItemDto create(ItemDto itemDto, int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользоваетля с id " + userId + " не существует"));
        itemDto.setOwner(userMapper.convertUser(user));
        Item newItem = itemMapper.convertItemDto(itemDto);
        return itemMapper.convertItem(itemRepository.save(newItem));
    }

    @Override
    public ItemDto update(ItemDto itemDto, int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользоваетля с id " + userId + " не существует"));
        itemDto.setOwner(userMapper.convertUser(user));
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
            setBookingsToItemDto(itemWithBookingsDto);
        }
        setCommentsToItemDto(itemWithBookingsDto);
        return itemWithBookingsDto;
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        List<Item> items = itemRepository.search(text);
        return itemMapper.convertListItem(items);
    }

    private void setBookingsToItemDto(ItemWithBookingsAndCommentsDto itemWithBookingsDto) {
        List<Booking> bookings = bookingRepository.findAllByItemId(itemWithBookingsDto.getId());
        Optional<Booking> lastBooking = bookings.stream()
                .filter(e -> e.getStartDate().isBefore(LocalDateTime.now()) && e.getStatus().equals(BookingState.APPROVED))
                .max(Comparator.comparing(Booking::getStartDate));
        Optional<Booking> nextBooking = bookings.stream()
                .filter(e -> e.getStartDate().isAfter(LocalDateTime.now()) && e.getStatus().equals(BookingState.APPROVED))
                .min(Comparator.comparing(Booking::getStartDate));
        BookingShortDto lastBookingDto = lastBooking.map(bookingMapper::convertBookingToShortDto).orElse(null);
        itemWithBookingsDto.setLastBooking(lastBookingDto);
        BookingShortDto nextBookingDto = nextBooking.map(bookingMapper::convertBookingToShortDto).orElse(null);
        itemWithBookingsDto.setNextBooking(nextBookingDto);
    }

    private void setCommentsToItemDto(ItemWithBookingsAndCommentsDto itemWithBookingsDto) {
        List<Comment> comments = commentRepository.findAllByItemIdOrderByCreatedDesc(itemWithBookingsDto.getId());
        itemWithBookingsDto.setComments(comments.stream()
                .map(commentMapper::convertComment)
                .collect(Collectors.toList()));
    }
}
