package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
public class RequestIT {
    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserService userService;

    private User testUser;
    private User testUser2;
    private UserDto testUserDto;
    private UserDto testUserDto2;
    private UserDto testWrongUserDto;
    private ItemRequestCreateDto requestCreateDto1;
    private ItemRequestCreateDto requestCreateDto2;
    private ItemRequestCreateDto requestCreateDtoWithWrongUser;
    private ItemRequestDto itemRequestDto;
    private ItemRequestDto wrongItemRequestDto;
    private ItemRequestDto itemRequestDtoWithItems;

    @BeforeEach
    void setup() {
        testUser = User.builder()
                .name("test name")
                .email("test@email.com")
                .build();
        testUser2 = User.builder()
                .name("test name2")
                .email("test2@email.com")
                .build();
        testUserDto = UserDto.builder()
                .name("test name")
                .email("test@email.com")
                .build();
        testUserDto2 = UserDto.builder()
                .name("test name2")
                .email("test2@email.com")
                .build();
        testWrongUserDto = UserDto.builder()
                .name("test name2")
                .email("test2@email.com")
                .id(66)
                .build();
        testUserDto = userService.create(testUserDto);
        testUserDto2 = userService.create(testUserDto2);

        requestCreateDto1 = ItemRequestCreateDto.builder()
                .requesterId(testUserDto.getId())
                .description("test description")
                .created(LocalDateTime.now())
                .build();
        requestCreateDto2 = ItemRequestCreateDto.builder()
                .requesterId(testUserDto.getId())
                .description("test description")
                .created(LocalDateTime.now())
                .build();
        itemRequestDto = itemRequestService.create(requestCreateDto2);
        itemRequestDtoWithItems = itemRequestDto.toBuilder().items(List.of()).build();
        requestCreateDtoWithWrongUser = ItemRequestCreateDto.builder()
                .requesterId(testWrongUserDto.getId())
                .description("test description")
                .created(LocalDateTime.now())
                .build();
        wrongItemRequestDto = ItemRequestDto.builder()
                .id(66)
                .description("test description")
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void createRequest_whenSuccessful_thenReturnItemRequestDto() {
        ItemRequestDto actualItemRequestDto = itemRequestService.create(requestCreateDto1);

        assertThat(actualItemRequestDto.getId(), notNullValue());
        assertThat(itemRequestDto.getDescription(), equalTo(actualItemRequestDto.getDescription()));
    }

    @Test
    void createRequest_whenNoSuchUser_thenThrownException() {
        assertThrows(NotFoundException.class,
                () -> itemRequestService.create(requestCreateDtoWithWrongUser));
    }

    @Test
    void getAllOwn_whenSuccessful_thenReturnListOfItemRequestDto() {
        List<ItemRequestDto> itemRequestDtoList = itemRequestService.getAllOwn(testUserDto.getId());

        assertThat(itemRequestDtoList, iterableWithSize(1));
        assertThat(itemRequestDtoList, contains(itemRequestDtoWithItems));
    }

    @Test
    void getAllOwn_whenUserNotFound_thenThrownException() {
        assertThrows(NotFoundException.class, () -> itemRequestService.getAllOwn(testWrongUserDto.getId()));
    }

    @Test
    void getAll_whenSuccessful_thenReturnListOfItemRequestDto() {
        List<ItemRequestDto> expectedItemRequestListDtos = List.of(itemRequestDtoWithItems);

        List<ItemRequestDto> actualItemRequestListDtos = itemRequestService.getAll(testUserDto2.getId(),
                0,2);

        assertThat(actualItemRequestListDtos, iterableWithSize(1));
        assertThat(actualItemRequestListDtos, contains(itemRequestDtoWithItems));
        assertThat(expectedItemRequestListDtos, equalTo(actualItemRequestListDtos));
    }

    @Test
    void getAll_whenUserNotFound_thenThrownException() {
        assertThrows(NotFoundException.class, () -> itemRequestService.getAll(testWrongUserDto.getId(), 0,2));
    }

    @Test
    void get_whenSuccessful_thenReturnItemRequestDto() {
        ItemRequestDto actualItemRequestDto = itemRequestService.get(testUserDto.getId(), itemRequestDto.getId());

        assertThat(actualItemRequestDto, is(itemRequestDto));
    }

    @Test
    void get_whenUserNotFound_thenThrownException() {
        assertThrows(NotFoundException.class, () -> itemRequestService.get(testWrongUserDto.getId(), itemRequestDto.getId()));
    }

    @Test
    void get_whenRequestNotFound_thenThrownException() {
        assertThrows(NotFoundException.class,
                () -> itemRequestService.get(testUserDto.getId(), wrongItemRequestDto.getId()));
    }
}
