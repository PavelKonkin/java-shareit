package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;

@Service
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final ItemMapper itemMapper;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository, UserRepository userRepository,
                              BookingRepository bookingRepository, ItemRepository itemRepository,
                              CommentMapper commentMapper, UserMapper userMapper, ItemMapper itemMapper) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
        this.itemMapper = itemMapper;
    }

    @Override
    public CommentDto create(CommentCreateDto commentCreateDto) {
        int userId = commentCreateDto.getAuthorId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с id "
                        + userId + " не существует"));
        int itemId = commentCreateDto.getItemId();
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Вещи с id " + itemId + " не существует"));
        Booking bookings = bookingRepository.findFirstByBookerIdAndItemIdAndStatusAndEndDateIsBefore(userId, itemId,
                BookingState.APPROVED, commentCreateDto.getCreated())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь " + user
                        + " не брал вещь с id " + itemId));
        CommentDto commentDto = CommentDto.builder()
                .created(commentCreateDto.getCreated())
                .text(commentCreateDto.getText())
                .author(userMapper.convertUser(user))
                .item(itemMapper.convertItem(item))
                .build();
        Comment comment = commentRepository.save(commentMapper.convertDto(commentDto));
        return commentMapper.convertComment(comment);
    }
}
