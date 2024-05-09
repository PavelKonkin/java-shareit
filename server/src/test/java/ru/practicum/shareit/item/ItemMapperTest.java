package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsAndCommentsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
public class ItemMapperTest {
    @InjectMocks
    private ItemMapper itemMapper;

    private ItemDto itemDto;
    private ItemDto itemDtoWithNoRequest;
    private Item item;
    private Item itemWithNoRequest;
    private ItemWithBookingsAndCommentsDto itemWithBookingsAndCommentsDto;
    private ItemRequest itemRequest;
    private User user;

    @BeforeEach
    void setup() {
        user = User.builder()
                .id(1L)
                .name("test user")
                .email("test@user.email")
                .build();
        itemRequest = ItemRequest.builder()
                .id(1L)
                .created(LocalDateTime.now())
                .requester(user)
                .description("description")
                .build();
        item = Item.builder()
                .name("test item")
                .description("test description")
                .available(true)
                .id(1L)
                .request(itemRequest)
                .build();
        itemWithNoRequest = Item.builder()
                .name("test item2")
                .description("test description2")
                .available(true)
                .id(2L)
                .build();
        itemDtoWithNoRequest = ItemDto.builder()
                .id(itemWithNoRequest.getId())
                .available(itemWithNoRequest.getAvailable())
                .name(itemWithNoRequest.getName())
                .description(itemWithNoRequest.getDescription())
                .build();
        itemDto = ItemDto.builder()
                .id(item.getId())
                .available(item.getAvailable())
                .name(item.getName())
                .description(item.getDescription())
                .requestId(itemRequest.getId())
                .build();
        itemWithBookingsAndCommentsDto = ItemWithBookingsAndCommentsDto.builder()
                .id(item.getId())
                .available(item.getAvailable())
                .name(item.getName())
                .description(item.getDescription())
                .build();
    }

    @Test
    void convertItem_whenItemHasRequest_thenReturnItemDtoWithRequestId() {
        ItemDto actualItemdto = itemMapper.convertItem(item);

        assertThat(itemDto, is(actualItemdto));
    }

    @Test
    void convertItem_whenItemWithoutRequest_thenReturnItemDtoWithoutRequestId() {
        ItemDto actualItemdto = itemMapper.convertItem(itemWithNoRequest);

        assertThat(itemDtoWithNoRequest, is(actualItemdto));
    }

    @Test
    void convertItemDto_whenSuccessful_thenReturnItem() {
        Item actualItem = itemMapper.convertItemDto(itemDto);

        assertThat(item, is(actualItem));
    }

    @Test
    void convertItemToBookingDto_whenSuccessful_thenReturnItemBookingDto() {
        ItemWithBookingsAndCommentsDto actualItemDto = itemMapper.convertItemToBookingDto(item);

        assertThat(itemWithBookingsAndCommentsDto, is(actualItemDto));
    }

    @Test
    void convertListItem_whenSuccessful_thenReturnListOfItemDto() {
        List<ItemDto> actualListOfItemDto = itemMapper.convertListItem(List.of(item));

        assertThat(List.of(itemDto), is(actualListOfItemDto));
    }
}
