package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.page.OffsetPage;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequestServiceTest {
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRequestMapper itemRequestMapper;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemMapper itemMapper;
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;
    private  User user;
    private  User user2;
    private  User notExistentUser;
    private ItemRequest itemRequest1;
    private ItemRequest itemRequest2;
    private ItemRequest itemRequestCreate;
    private ItemRequestDto itemRequestDto1;
    private ItemRequestDto itemRequestDto2;
    private ItemRequestDto itemRequestWithItemsDto1;
    private ItemRequestDto itemRequestWithItemsDto2;
    private ItemRequestCreateDto requestCreateDto;
    private ItemRequestCreateDto requestCreateDtoWithWrongUser;
    private Item item1;
    private Item item2;
    private ItemDto itemDto1;
    private ItemDto itemDto2;
    private final Sort sort = Sort.by("created");
    private final Pageable page = new OffsetPage(0, 10, sort);


    @BeforeEach
    void setup() {
        user = User.builder()
                .id(1L)
                .name("user test 1")
                .email("test@user.test")
                .build();
        user2 = User.builder()
                .id(2L)
                .name("user test 2")
                .email("test@user2.test")
                .build();
        notExistentUser = User.builder()
                .id(66L)
                .name("user test 66")
                .email("test@user66.test")
                .build();
        itemRequest1 = ItemRequest.builder()
                .id(1L)
                .description("request test 1")
                .created(LocalDateTime.now())
                .requester(user)
                .build();
        itemRequest2 = ItemRequest.builder()
                .id(2L)
                .description("request test 2")
                .created(LocalDateTime.now())
                .requester(user)
                .build();
        itemRequestCreate = ItemRequest.builder()
                .description("request test 1")
                .created(LocalDateTime.now())
                .requester(user)
                .build();
        itemRequestDto1 = ItemRequestDto.builder()
                .id(1L)
                .description("request test 1")
                .created(itemRequest1.getCreated())
                .items(List.of())
                .build();
        itemRequestDto2 = ItemRequestDto.builder()
                .id(2L)
                .description("request test 2")
                .created(itemRequest2.getCreated())
                .items(List.of())
                .build();
        itemDto1 = ItemDto.builder()
                .id(1L)
                .requestId(1L)
                .name("test item 1")
                .description("test item description 1")
                .available(true)
                .build();
        itemDto2 = ItemDto.builder()
                .id(2L)
                .requestId(2L)
                .name("test item 2")
                .description("test item description 2")
                .available(true)
                .build();
        itemRequestWithItemsDto1 = ItemRequestDto.builder()
                .id(1L)
                .description("request test 1")
                .created(itemRequest1.getCreated())
                .items(List.of(itemDto1))
                .build();
        itemRequestWithItemsDto2 = ItemRequestDto.builder()
                .id(2L)
                .description("request test 2")
                .created(itemRequest2.getCreated())
                .items(List.of(itemDto2))
                .build();
        requestCreateDto = ItemRequestCreateDto.builder()
                .description("request test 1")
                .created(itemRequest1.getCreated())
                .requesterId(1L)
                .build();
        requestCreateDtoWithWrongUser = ItemRequestCreateDto.builder()
                .description("request test 66")
                .created(itemRequest1.getCreated())
                .requesterId(notExistentUser.getId())
                .build();
        item1 = Item.builder()
                .id(1L)
                .request(itemRequest1)
                .owner(user)
                .available(true)
                .name("test item 1")
                .description("test item description 1")
                .build();
        item2 = Item.builder()
                .id(2L)
                .request(itemRequest2)
                .owner(user)
                .available(true)
                .name("test item 2")
                .description("test item description 2")
                .build();
    }


    @Test
    void create_whenSuccessful_thenReturnItemRequestDto() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRequestRepository.save(itemRequestCreate)).thenReturn(itemRequest1);
        when(itemRequestMapper.convertRequest(itemRequest1)).thenReturn(itemRequestDto1);

        ItemRequestDto actualItemRequestDto = itemRequestService.create(requestCreateDto);

        verify(itemRequestMapper, times(1)).convertRequest(itemRequest1);
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).save(itemRequestCreate);
        assertThat(itemRequestDto1, is(actualItemRequestDto));
    }

    @Test
    void create_whenUserNotFound_thenThrownException() {
        when(userRepository.findById(notExistentUser.getId())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemRequestService.create(requestCreateDtoWithWrongUser)
        );

        verify(itemRequestMapper, times(0)).convertRequest(any());
        verify(userRepository, times(1)).findById(notExistentUser.getId());
        verify(itemRequestRepository, times(0)).save(any());
        assertEquals("Пользователя с id " + notExistentUser.getId() + " не существует", exception.getMessage());
    }

    @Test
    void getAllOwn_whenThereAreItemRequestsAndNoItems_thenReturnListOfItemRequestDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAllByRequesterId(1L, sort))
                .thenReturn(List.of(itemRequest1, itemRequest2));
        when(itemRepository.findAllByRequestsId(any())).thenReturn(List.of());
        when(itemRequestMapper.convertRequest(itemRequest1)).thenReturn(itemRequestDto1);
        when(itemRequestMapper.convertRequest(itemRequest2)).thenReturn(itemRequestDto2);
        when(itemMapper.convertListItem(List.of())).thenReturn(List.of());
        List<ItemRequestDto> expectedItemRequestsDto = List.of(itemRequestDto1, itemRequestDto2);

        List<ItemRequestDto> actualItemRequestsDto = itemRequestService.getAllOwn(1, sort);

        assertThat(actualItemRequestsDto, is(expectedItemRequestsDto));
        verify(itemRequestMapper, times(2)).convertRequest(any());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).findAllByRequesterId(1L, sort);
        verify(itemRepository, times(1)).findAllByRequestsId(any());
        verify(itemMapper, times(2)).convertListItem(any());
    }

    @Test
    void getAllOwn_whenThereAreItemRequestsAndItems_thenReturnListOfItemRequestDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAllByRequesterId(1L, sort))
                .thenReturn(List.of(itemRequest1, itemRequest2));
        when(itemRepository.findAllByRequestsId(List.of(1L, 2L))).thenReturn(List.of(item1, item2));
        when(itemRequestMapper.convertRequest(itemRequest1)).thenReturn(itemRequestDto1);
        when(itemRequestMapper.convertRequest(itemRequest2)).thenReturn(itemRequestDto2);
        when(itemMapper.convertListItem(List.of(item1))).thenReturn(List.of(itemDto1));
        when(itemMapper.convertListItem(List.of(item2))).thenReturn(List.of(itemDto2));
        List<ItemRequestDto> expectedItemRequestsDto = List.of(itemRequestWithItemsDto1, itemRequestWithItemsDto2);

        List<ItemRequestDto> actualItemRequestsDto = itemRequestService.getAllOwn(1, sort);

        assertThat(actualItemRequestsDto, is(expectedItemRequestsDto));
        verify(itemRequestMapper, times(2)).convertRequest(any());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).findAllByRequesterId(1L, sort);
        verify(itemRepository, times(1)).findAllByRequestsId(any());
        verify(itemMapper, times(2)).convertListItem(any());
    }

    @Test
    void getAllOwn_whenUserHasNoRequest_thenReturnEmptyList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAllByRequesterId(1L, sort))
                .thenReturn(List.of());
        List<ItemRequestDto> expectedItemRequestsDto = List.of();

        List<ItemRequestDto> actualItemRequestsDto = itemRequestService.getAllOwn(1L, sort);

        assertThat(actualItemRequestsDto, is(expectedItemRequestsDto));
        verify(itemRequestMapper, times(0)).convertRequest(any());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).findAllByRequesterId(1L, sort);
        verify(itemRepository, times(1)).findAllByRequestsId(any());
        verify(itemMapper, times(0)).convertItem(any());
    }

    @Test
    void getAllOwn_whenUserNotFound_thenThrownException() {
        when(userRepository.findById(notExistentUser.getId())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemRequestService.getAllOwn(notExistentUser.getId(), sort)
        );

        verify(itemRequestMapper, times(0)).convertRequest(any());
        verify(userRepository, times(1)).findById(notExistentUser.getId());
        verify(itemRequestRepository, times(0)).findAllByRequesterId(anyInt(), any());
        verify(itemRepository, times(0)).findAllByRequestsId(any());
        verify(itemMapper, times(0)).convertItem(any());
        assertEquals("Пользователя с id " + notExistentUser.getId() + " не существует", exception.getMessage());
    }

    @Test
    void getAll_whenThereAreRequestsWithoutItems_thenReturnListOfItemRequestDto() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(itemRequestRepository.findAllByRequesterIdIsNot(2, page))
                .thenReturn(List.of(itemRequest1, itemRequest2));
        when(itemRepository.findAllByRequestsId(List.of(1L, 2L))).thenReturn(List.of());
        when(itemRequestMapper.convertRequest(itemRequest1)).thenReturn(itemRequestDto1);
        when(itemRequestMapper.convertRequest(itemRequest2)).thenReturn(itemRequestDto2);

        List<ItemRequestDto> actualItemRequestDtos = itemRequestService.getAll(2L, page);

        assertThat(List.of(itemRequestDto1, itemRequestDto2), is(actualItemRequestDtos));
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).findAllByRequesterIdIsNot(2L, page);
        verify(itemRepository, times(1)).findAllByRequestsId(anyList());
        verify(itemMapper, times(2)).convertListItem(anyList());
        verify(itemRequestMapper, times(2)).convertRequest(any(ItemRequest.class));
    }

    @Test
    void getAll_whenThereAreRequestsWithItems_thenReturnListOfItemRequestDtoWithItems() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(itemRequestRepository.findAllByRequesterIdIsNot(2L, page))
                .thenReturn(List.of(itemRequest1, itemRequest2));
        when(itemRepository.findAllByRequestsId(List.of(1L, 2L))).thenReturn(List.of(item1, item2));
        when(itemMapper.convertListItem(List.of(item1))).thenReturn(List.of(itemDto1));
        when(itemMapper.convertListItem(List.of(item2))).thenReturn(List.of(itemDto2));
        when(itemRequestMapper.convertRequest(itemRequest1)).thenReturn(itemRequestDto1);
        when(itemRequestMapper.convertRequest(itemRequest2)).thenReturn(itemRequestDto2);

        List<ItemRequestDto> actualItemRequestDtos = itemRequestService.getAll(2L, page);

        assertThat(List.of(itemRequestWithItemsDto1, itemRequestWithItemsDto2), equalTo(actualItemRequestDtos));
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).findAllByRequesterIdIsNot(2L, page);
        verify(itemRepository, times(1)).findAllByRequestsId(anyList());
        verify(itemMapper, times(2)).convertListItem(anyList());
        verify(itemRequestMapper, times(2)).convertRequest(any(ItemRequest.class));
    }

    @Test
    void getAll_whenThereAreNoOtherRequest_thenReturnEmptyList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAllByRequesterIdIsNot(1L, page))
                .thenReturn(List.of());
        when(itemRepository.findAllByRequestsId(List.of())).thenReturn(List.of());

        List<ItemRequestDto> actualItemRequestDtos = itemRequestService.getAll(1L, page);

        assertThat(List.of(), is(actualItemRequestDtos));
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).findAllByRequesterIdIsNot(1L, page);
        verify(itemRepository, times(1)).findAllByRequestsId(anyList());
        verify(itemMapper, times(0)).convertListItem(anyList());
        verify(itemRequestMapper, times(0)).convertRequest(any(ItemRequest.class));
    }

    @Test
    void getAll_whenUserNotFound_thenThrownException() {
        when(userRepository.findById(notExistentUser.getId())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemRequestService.getAll(notExistentUser.getId(), page)
        );

        verify(itemRequestMapper, times(0)).convertRequest(any(ItemRequest.class));
        verify(userRepository, times(1)).findById(notExistentUser.getId());
        verify(itemRequestRepository, times(0)).findAllByRequesterIdIsNot(anyLong(), any());
        verify(itemRepository, times(0)).findAllByRequestsId(any());
        verify(itemMapper, times(0)).convertListItem(anyList());
        assertEquals("Пользователя с id " + notExistentUser.getId() + " не существует", exception.getMessage());
    }

    @Test
    void get_whenFound_thenReturnItemRequestDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(itemRequest1));
        when(itemRepository.findAllByRequestId(1L)).thenReturn(List.of(item1));
        when(itemMapper.convertListItem(List.of(item1))).thenReturn(List.of(itemDto1));
        when(itemRequestMapper.convertRequest(itemRequest1)).thenReturn(itemRequestDto1);

        ItemRequestDto actualItemRequestDto = itemRequestService.get(1L, 1);

        assertThat(itemRequestWithItemsDto1, equalTo(actualItemRequestDto));
        verify(userRepository, times(1)).findById(1L);
        verify(itemRequestRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).findAllByRequestId(1L);
        verify(itemMapper, times(1)).convertListItem(anyList());
        verify(itemRequestMapper, times(1)).convertRequest(itemRequest1);
    }

    @Test
    void get_whenUserNotFound_thenThrownException() {
        when(userRepository.findById(notExistentUser.getId())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemRequestService.get(notExistentUser.getId(), 1L)
        );

        verify(userRepository, times(1)).findById(notExistentUser.getId());
        verify(itemRequestRepository, times(0)).findById(1L);
        verify(itemRepository, times(0)).findAllByRequestId(1L);
        verify(itemMapper, times(0)).convertListItem(anyList());
        verify(itemRequestMapper, times(0)).convertRequest(itemRequest1);
        assertEquals("Пользователя с id " + notExistentUser.getId() + " не существует", exception.getMessage());
    }

    @Test
    void get_whenRequestNotFound_thenThrownException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(3L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemRequestService.get(user.getId(), 3L)
        );

        verify(userRepository, times(1)).findById(user.getId());
        verify(itemRequestRepository, times(1)).findById(3L);
        verify(itemRepository, times(0)).findAllByRequestId(1L);
        verify(itemMapper, times(0)).convertListItem(anyList());
        verify(itemRequestMapper, times(0)).convertRequest(itemRequest1);
        assertEquals("Запроса с id " + 3 + " не существует", exception.getMessage());
    }
}

