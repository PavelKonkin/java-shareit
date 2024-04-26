package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingStateDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingMapper bookingMapper;
    @InjectMocks
    private BookingServiceImpl bookingService;

    private BookingCreateDto bookingCreateDto;
    private Booking booking;
    private Booking bookingSaved;
    private BookingDto bookingDto;
    private Item item;
    private ItemDto itemDto;
    private User user;
    private User user2;
    private UserDto userDto;
    private UserDto userDto2;
    private final Sort sort = Sort.by("id").descending();
    private final PageRequest page = PageRequest.of(0, 10, sort);

    @BeforeEach
    void setup() {
        user = User.builder()
                .id(1)
                .name("test user")
                .email("test@user.email")
                .build();
        user2 = User.builder()
                .id(2)
                .name("test user2")
                .email("test@user2.email")
                .build();
        userDto = UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
        userDto2 = UserDto.builder()
                .id(user2.getId())
                .name(user2.getName())
                .email(user2.getEmail())
                .build();
        item = Item.builder()
                .id(1)
                .name("test item")
                .description("test description")
                .owner(user)
                .available(true)
                .requestId(1)
                .build();
        itemDto = ItemDto.builder()
                .id(item.getId())
                .available(item.getAvailable())
                .name(item.getName())
                .description(item.getDescription())
                .requestId(item.getRequestId())
                .owner(userDto)
                .build();
        bookingCreateDto = BookingCreateDto.builder()
                .itemId(item.getId())
                .bookerId(user2.getId())
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(1))
                .status(BookingStateDto.WAITING)
                .build();
        booking = Booking.builder()
                .booker(user2)
                .item(item)
                .status(BookingState.WAITING)
                .startDate(bookingCreateDto.getStart())
                .endDate(bookingCreateDto.getEnd())
                .build();
        bookingSaved = booking.toBuilder().id(1).build();
        bookingDto = BookingDto.builder()
                .id(booking.getId())
                .booker(userDto2)
                .item(itemDto)
                .status(BookingStateDto.WAITING)
                .start(booking.getStartDate())
                .end(booking.getEndDate())
                .build();
    }

    @Test
    void create_whenSuccessful_thenReturnBookingDto() {
        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.save(booking)).thenReturn(bookingSaved);
        when(bookingMapper.convertBooking(bookingSaved)).thenReturn(bookingDto);

        BookingDto actualBookingDto = bookingService.create(bookingCreateDto);

        assertThat(bookingDto, is(actualBookingDto));
        verify(userRepository, times(1)).findById(user2.getId());
        verify(itemRepository, times(1)).findById(item.getId());
        verify(bookingRepository, times(1)).save(booking);
        verify(bookingMapper, times(1)).convertBooking(bookingSaved);
    }

    @Test
    void create_whenUserNotFound_thenThrownException() {
        int wrongUserId = 66;
        bookingCreateDto.setBookerId(wrongUserId);
        when(userRepository.findById(wrongUserId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.create(bookingCreateDto)
        );
        assertThat(exception.getMessage(), is("Пользователя с id " + wrongUserId + " не существует"));
        verify(userRepository, times(1)).findById(wrongUserId);
        verify(itemRepository, never()).findById(anyInt());
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(bookingMapper, never()).convertBooking(any(Booking.class));
    }

    @Test
    void create_whenItemNotFound_thenThrownException() {
        int wrongItemId = 66;
        bookingCreateDto.setItemId(wrongItemId);
        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        when(itemRepository.findById(wrongItemId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.create(bookingCreateDto)
        );
        assertThat(exception.getMessage(), is("вещи с id " + wrongItemId + " не существует"));
        verify(userRepository, times(1)).findById(user2.getId());
        verify(itemRepository, times(1)).findById(wrongItemId);
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(bookingMapper, never()).convertBooking(any(Booking.class));
    }

    @Test
    void create_whenBookerIsOwner_thenThrownException() {
        bookingCreateDto.setBookerId(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.create(bookingCreateDto)
        );
        assertThat(exception.getMessage(), is("Владелец вещи не может брать у себя её в аренду"));
        verify(userRepository, times(1)).findById(user.getId());
        verify(itemRepository, times(1)).findById(item.getId());
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(bookingMapper, never()).convertBooking(any(Booking.class));
    }

    @Test
    void create_whenItemNotAvailable_thenThrownException() {
        item.setAvailable(false);
        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.create(bookingCreateDto)
        );
        assertThat(exception.getMessage(), is("Вещь " + item + " недоступна для бронирования"));
        verify(userRepository, times(1)).findById(user2.getId());
        verify(itemRepository, times(1)).findById(item.getId());
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(bookingMapper, never()).convertBooking(any(Booking.class));
    }

    @Test
    void confirmReject_whenConfirm_thenReturnConfirmedBookingDto() {
        Booking bookingSavedApproved = bookingSaved.toBuilder().status(BookingState.APPROVED).build();
        when(bookingRepository.findById(bookingSaved.getId())).thenReturn(Optional.of(bookingSaved));
        when(bookingRepository.save(bookingSavedApproved)).thenReturn(bookingSavedApproved);
        bookingDto.setStatus(BookingStateDto.APPROVED);
        when(bookingMapper.convertBooking(bookingSavedApproved)).thenReturn(bookingDto);

        BookingDto actualBookingDto = bookingService
                .confirmReject(user.getId(), bookingSaved.getId(), "true");

        assertThat(bookingDto, is(actualBookingDto));
        verify(bookingRepository, times(1)).findById(bookingSaved.getId());
        verify(bookingRepository, times(1)).save(bookingSavedApproved);
        verify(bookingMapper, times(1)).convertBooking(bookingSavedApproved);
    }

    @Test
    void confirmReject_whenReject_thenReturnRejectedBookingDto() {
        Booking bookingSavedApproved = bookingSaved.toBuilder().status(BookingState.REJECTED).build();
        when(bookingRepository.findById(bookingSaved.getId())).thenReturn(Optional.of(bookingSaved));
        when(bookingRepository.save(bookingSavedApproved)).thenReturn(bookingSavedApproved);
        bookingDto.setStatus(BookingStateDto.REJECTED);
        when(bookingMapper.convertBooking(bookingSavedApproved)).thenReturn(bookingDto);

        BookingDto actualBookingDto = bookingService
                .confirmReject(user.getId(), bookingSaved.getId(), "false");

        assertThat(bookingDto, is(actualBookingDto));
        verify(bookingRepository, times(1)).findById(bookingSaved.getId());
        verify(bookingRepository, times(1)).save(bookingSavedApproved);
        verify(bookingMapper, times(1)).convertBooking(bookingSavedApproved);
    }

    @Test
    void confirmReject_whenItemNotFound_thenThrownException() {
        int wrongBookingId = 66;
        when(bookingRepository.findById(wrongBookingId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.confirmReject(user.getId(), wrongBookingId, "true")
        );
        assertThat(exception.getMessage(), is("Бронирования с id " + wrongBookingId + " не существует"));
        verify(bookingRepository, times(1)).findById(wrongBookingId);
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(bookingMapper, never()).convertBooking(any(Booking.class));
    }

    @Test
    void confirmReject_whenUserNotOwner_thenThrownException() {
        when(bookingRepository.findById(bookingSaved.getId())).thenReturn(Optional.of(bookingSaved));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.confirmReject(user2.getId(), bookingSaved.getId(), "true")
        );
        assertThat(exception.getMessage(), is("Попытка изменить статус бронирования вещи с id "
                + bookingSaved.getItem().getId() + " пользователем с id " + user2.getId() +
                ", не являющимся владельцем вещи "));
        verify(bookingRepository, times(1)).findById(bookingSaved.getId());
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(bookingMapper, never()).convertBooking(any(Booking.class));
    }

    @Test
    void confirmReject_whenAlreadyConfirmed_thenThrownException() {
        bookingSaved.setStatus(BookingState.APPROVED);
        when(bookingRepository.findById(bookingSaved.getId())).thenReturn(Optional.of(bookingSaved));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.confirmReject(user.getId(), bookingSaved.getId(), "true")
        );
        assertThat(exception.getMessage(), is("Попытка повторного подтверждения или отказа в бронировании "
                + bookingSaved));
        verify(bookingRepository, times(1)).findById(bookingSaved.getId());
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(bookingMapper, never()).convertBooking(any(Booking.class));
    }

    @Test
    void get_whenUserIsOwner_thenReturnBookingDto() {
        when(bookingRepository.getByIdAndBookerIdOrItemOwnerId(bookingSaved.getId(), user.getId()))
                .thenReturn(Optional.of(bookingSaved));
        when(bookingMapper.convertBooking(bookingSaved)).thenReturn(bookingDto);

        BookingDto actualBookingDto = bookingService.get(user.getId(), bookingSaved.getId());

        assertThat(bookingDto, is(actualBookingDto));
        verify(bookingRepository, times(1))
                .getByIdAndBookerIdOrItemOwnerId(bookingSaved.getId(), user.getId());
        verify(bookingMapper, times(1)).convertBooking(bookingSaved);
    }

    @Test
    void get_whenUserIsBooker_thenReturnBookingDto() {
        when(bookingRepository.getByIdAndBookerIdOrItemOwnerId(bookingSaved.getId(), user2.getId()))
                .thenReturn(Optional.of(bookingSaved));
        when(bookingMapper.convertBooking(bookingSaved)).thenReturn(bookingDto);

        BookingDto actualBookingDto = bookingService.get(user2.getId(), bookingSaved.getId());

        assertThat(bookingDto, is(actualBookingDto));
        verify(bookingRepository, times(1))
                .getByIdAndBookerIdOrItemOwnerId(bookingSaved.getId(), user2.getId());
        verify(bookingMapper, times(1)).convertBooking(bookingSaved);
    }

    @Test
    void get_whenBookingNotFound_thenThrownException() {
        int wrongBookingId = 66;
        bookingSaved.setId(wrongBookingId);
        when(bookingRepository.getByIdAndBookerIdOrItemOwnerId(wrongBookingId, user2.getId()))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.get(user2.getId(), bookingSaved.getId())
        );
        assertThat(exception.getMessage(), is("Бронирования с id " + wrongBookingId + " с создателем с id "
                + user2.getId() + " или id владельца вещи " + user2.getId() + " не найдено"));
        verify(bookingRepository, times(1))
                .getByIdAndBookerIdOrItemOwnerId(bookingSaved.getId(), user2.getId());
        verify(bookingMapper, never()).convertBooking(any(Booking.class));
    }

    @Test
    void getAllForBooker_whenWaitingState_thenReturnListOfBookingDto() {
        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        when(bookingRepository.findAllByBookerIdAndStatus(user2.getId(), BookingState.WAITING, page))
                .thenReturn(new PageImpl<>(List.of(bookingSaved)));
        when(bookingMapper.convertBooking(bookingSaved)).thenReturn(bookingDto);

        List<BookingDto> actualListOfBookingDto = bookingService
                .getAllForBooker(user2.getId(), BookingStateDto.WAITING, 0, 10);

        assertThat(List.of(bookingDto), is(actualListOfBookingDto));
        verify(userRepository, times(1)).findById(user2.getId());
        verify(bookingRepository, times(1))
                .findAllByBookerIdAndStatus(user2.getId(), BookingState.WAITING, page);
        verify(bookingMapper, times(1)).convertBooking(bookingSaved);
    }

    @Test
    void getAllForBooker_whenAllState_thenReturnListOfBookingDto() {
        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        when(bookingRepository.findAllByBookerId(user2.getId(), page))
                .thenReturn(new PageImpl<>(List.of(bookingSaved)));
        when(bookingMapper.convertBooking(bookingSaved)).thenReturn(bookingDto);

        List<BookingDto> actualListOfBookingDto = bookingService
                .getAllForBooker(user2.getId(), BookingStateDto.ALL, 0, 10);

        assertThat(List.of(bookingDto), is(actualListOfBookingDto));
        verify(userRepository, times(1)).findById(user2.getId());
        verify(bookingRepository, times(1))
                .findAllByBookerId(user2.getId(), page);
        verify(bookingMapper, times(1)).convertBooking(bookingSaved);
    }

    @Test
    void getAllForBooker_whenFutureState_thenReturnListOfBookingDto() {
        LocalDateTime now = LocalDateTime.now();
        bookingSaved.setStartDate(now.plusHours(1));
        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        when(bookingRepository
                .findAllByBookerIdAndStartDateAfter(anyInt(), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(bookingSaved)));
        when(bookingMapper.convertBooking(bookingSaved)).thenReturn(bookingDto);

        List<BookingDto> actualListOfBookingDto = bookingService
                .getAllForBooker(user2.getId(), BookingStateDto.FUTURE, 0, 10);

        assertThat(List.of(bookingDto), is(actualListOfBookingDto));
        verify(userRepository, times(1)).findById(user2.getId());
        verify(bookingRepository, times(1))
                .findAllByBookerIdAndStartDateAfter(anyInt(), any(LocalDateTime.class), any(PageRequest.class));
        verify(bookingMapper, times(1)).convertBooking(bookingSaved);
    }

    @Test
    void getAllForBooker_whenPastState_thenReturnListOfBookingDto() {
        LocalDateTime now = LocalDateTime.now();
        bookingSaved.setEndDate(now.minusHours(1));
        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        when(bookingRepository
                .findAllByBookerIdAndEndDateIsBefore(anyInt(), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(bookingSaved)));
        when(bookingMapper.convertBooking(bookingSaved)).thenReturn(bookingDto);

        List<BookingDto> actualListOfBookingDto = bookingService
                .getAllForBooker(user2.getId(), BookingStateDto.PAST, 0, 10);

        assertThat(List.of(bookingDto), is(actualListOfBookingDto));
        verify(userRepository, times(1)).findById(user2.getId());
        verify(bookingRepository, times(1))
                .findAllByBookerIdAndEndDateIsBefore(anyInt(), any(LocalDateTime.class), any(PageRequest.class));
        verify(bookingMapper, times(1)).convertBooking(bookingSaved);
    }

    @Test
    void getAllForBooker_whenRejectedState_thenReturnListOfBookingDto() {
        bookingSaved.setStatus(BookingState.REJECTED);
        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        when(bookingRepository
                .findAllByBookerIdAndStatus(user2.getId(), BookingState.REJECTED, page))
                .thenReturn(new PageImpl<>(List.of(bookingSaved)));
        when(bookingMapper.convertBooking(bookingSaved)).thenReturn(bookingDto);

        List<BookingDto> actualListOfBookingDto = bookingService
                .getAllForBooker(user2.getId(), BookingStateDto.REJECTED, 0, 10);

        assertThat(List.of(bookingDto), is(actualListOfBookingDto));
        verify(userRepository, times(1)).findById(user2.getId());
        verify(bookingRepository, times(1))
                .findAllByBookerIdAndStatus(user2.getId(), BookingState.REJECTED, page);
        verify(bookingMapper, times(1)).convertBooking(bookingSaved);
    }

    @Test
    void getAllForBooker_whenApprovedState_thenReturnListOfBookingDto() {
        bookingSaved.setStatus(BookingState.APPROVED);
        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        when(bookingRepository
                .findAllByBookerIdAndStatus(user2.getId(), BookingState.APPROVED, page))
                .thenReturn(new PageImpl<>(List.of(bookingSaved)));
        when(bookingMapper.convertBooking(bookingSaved)).thenReturn(bookingDto);

        List<BookingDto> actualListOfBookingDto = bookingService
                .getAllForBooker(user2.getId(), BookingStateDto.APPROVED, 0, 10);

        assertThat(List.of(bookingDto), is(actualListOfBookingDto));
        verify(userRepository, times(1)).findById(user2.getId());
        verify(bookingRepository, times(1))
                .findAllByBookerIdAndStatus(user2.getId(), BookingState.APPROVED, page);
        verify(bookingMapper, times(1)).convertBooking(bookingSaved);
    }

    @Test
    void getAllForBooker_whenCurrentState_thenReturnListOfBookingDto() {
        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        when(bookingRepository
                .findAllByBookerCurrent(anyInt(), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(bookingSaved)));
        when(bookingMapper.convertBooking(bookingSaved)).thenReturn(bookingDto);

        List<BookingDto> actualListOfBookingDto = bookingService
                .getAllForBooker(user2.getId(), BookingStateDto.CURRENT, 0, 10);

        assertThat(List.of(bookingDto), is(actualListOfBookingDto));
        verify(userRepository, times(1)).findById(user2.getId());
        verify(bookingRepository, times(1))
                .findAllByBookerCurrent(anyInt(), any(LocalDateTime.class), any(PageRequest.class));
        verify(bookingMapper, times(1)).convertBooking(bookingSaved);
    }

    @Test
    void getAllForBooker_whenUserNotFound_thenThrownException() {
        int wrongUserId = 66;
        when(userRepository.findById(wrongUserId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.getAllForBooker(wrongUserId, BookingStateDto.WAITING, 0, 10)
        );
        assertThat(exception.getMessage(), is("Пользователя с id "
                + wrongUserId + " не существует"));
        verify(userRepository, times(1)).findById(wrongUserId);
        verifyNoInteractions(bookingRepository);
        verify(bookingMapper, never()).convertBooking(any(Booking.class));
    }

    @Test
    void getAllForOwner_whenWaitingState_thenReturnListOfBookingDto() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByItemOwnerIdAndStatus(user.getId(), BookingState.WAITING, page))
                .thenReturn(new PageImpl<>(List.of(bookingSaved)));
        when(bookingMapper.convertBooking(bookingSaved)).thenReturn(bookingDto);

        List<BookingDto> actualListOfBookingDto = bookingService
                .getAllForOwner(user.getId(), BookingStateDto.WAITING, 0, 10);

        assertThat(List.of(bookingDto), is(actualListOfBookingDto));
        verify(userRepository, times(1)).findById(user.getId());
        verify(bookingRepository, times(1))
                .findAllByItemOwnerIdAndStatus(user.getId(), BookingState.WAITING, page);
        verify(bookingMapper, times(1)).convertBooking(bookingSaved);
    }

    @Test
    void getAllForOwner_whenAllState_thenReturnListOfBookingDto() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByItemOwnerId(user.getId(), page))
                .thenReturn(new PageImpl<>(List.of(bookingSaved)));
        when(bookingMapper.convertBooking(bookingSaved)).thenReturn(bookingDto);

        List<BookingDto> actualListOfBookingDto = bookingService
                .getAllForOwner(user.getId(), BookingStateDto.ALL, 0, 10);

        assertThat(List.of(bookingDto), is(actualListOfBookingDto));
        verify(userRepository, times(1)).findById(user.getId());
        verify(bookingRepository, times(1))
                .findAllByItemOwnerId(user.getId(), page);
        verify(bookingMapper, times(1)).convertBooking(bookingSaved);
    }

    @Test
    void getAllForOwner_whenFutureState_thenReturnListOfBookingDto() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository
                .findAllByItemOwnerIdAndStartDateAfter(anyInt(), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(bookingSaved)));
        when(bookingMapper.convertBooking(bookingSaved)).thenReturn(bookingDto);

        List<BookingDto> actualListOfBookingDto = bookingService
                .getAllForOwner(user.getId(), BookingStateDto.FUTURE, 0, 10);

        assertThat(List.of(bookingDto), is(actualListOfBookingDto));
        verify(userRepository, times(1)).findById(user.getId());
        verify(bookingRepository, times(1))
                .findAllByItemOwnerIdAndStartDateAfter(anyInt(), any(LocalDateTime.class), any(PageRequest.class));
        verify(bookingMapper, times(1)).convertBooking(bookingSaved);
    }

    @Test
    void getAllForOwner_whenPastState_thenReturnListOfBookingDto() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository
                .findAllByItemOwnerIdAndEndDateIsBefore(anyInt(), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(bookingSaved)));
        when(bookingMapper.convertBooking(bookingSaved)).thenReturn(bookingDto);

        List<BookingDto> actualListOfBookingDto = bookingService
                .getAllForOwner(user.getId(), BookingStateDto.PAST, 0, 10);

        assertThat(List.of(bookingDto), is(actualListOfBookingDto));
        verify(userRepository, times(1)).findById(user.getId());
        verify(bookingRepository, times(1))
                .findAllByItemOwnerIdAndEndDateIsBefore(anyInt(), any(LocalDateTime.class), any(PageRequest.class));
        verify(bookingMapper, times(1)).convertBooking(bookingSaved);
    }

    @Test
    void getAllForOwner_whenRejectedState_thenReturnListOfBookingDto() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository
                .findAllByItemOwnerIdAndStatus(user.getId(), BookingState.REJECTED, page))
                .thenReturn(new PageImpl<>(List.of(bookingSaved)));
        when(bookingMapper.convertBooking(bookingSaved)).thenReturn(bookingDto);

        List<BookingDto> actualListOfBookingDto = bookingService
                .getAllForOwner(user.getId(), BookingStateDto.REJECTED, 0, 10);

        assertThat(List.of(bookingDto), is(actualListOfBookingDto));
        verify(userRepository, times(1)).findById(user.getId());
        verify(bookingRepository, times(1))
                .findAllByItemOwnerIdAndStatus(user.getId(), BookingState.REJECTED, page);
        verify(bookingMapper, times(1)).convertBooking(bookingSaved);
    }

    @Test
    void getAllForOwner_whenApprovedState_thenReturnListOfBookingDto() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository
                .findAllByItemOwnerIdAndStatus(user.getId(), BookingState.APPROVED, page))
                .thenReturn(new PageImpl<>(List.of(bookingSaved)));
        when(bookingMapper.convertBooking(bookingSaved)).thenReturn(bookingDto);

        List<BookingDto> actualListOfBookingDto = bookingService
                .getAllForOwner(user.getId(), BookingStateDto.APPROVED, 0, 10);

        assertThat(List.of(bookingDto), is(actualListOfBookingDto));
        verify(userRepository, times(1)).findById(user.getId());
        verify(bookingRepository, times(1))
                .findAllByItemOwnerIdAndStatus(user.getId(), BookingState.APPROVED, page);
        verify(bookingMapper, times(1)).convertBooking(bookingSaved);
    }

    @Test
    void getAllForOwner_whenCurrentState_thenReturnListOfBookingDto() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository
                .findAllByOwnerCurrent(anyInt(), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(bookingSaved)));
        when(bookingMapper.convertBooking(bookingSaved)).thenReturn(bookingDto);

        List<BookingDto> actualListOfBookingDto = bookingService
                .getAllForOwner(user.getId(), BookingStateDto.CURRENT, 0, 10);

        assertThat(List.of(bookingDto), is(actualListOfBookingDto));
        verify(userRepository, times(1)).findById(user.getId());
        verify(bookingRepository, times(1))
                .findAllByOwnerCurrent(anyInt(), any(LocalDateTime.class), any(PageRequest.class));
        verify(bookingMapper, times(1)).convertBooking(bookingSaved);
    }

    @Test
    void getAllForOwner_whenUserNotFound_thenThrownException() {
        int wrongUserId = 66;
        when(userRepository.findById(wrongUserId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.getAllForOwner(wrongUserId, BookingStateDto.WAITING, 0, 10)
        );
        assertThat(exception.getMessage(), is("Пользователя с id "
                + wrongUserId + " не существует"));
        verify(userRepository, times(1)).findById(wrongUserId);
        verifyNoInteractions(bookingRepository);
        verify(bookingMapper, never()).convertBooking(any(Booking.class));
    }
}
