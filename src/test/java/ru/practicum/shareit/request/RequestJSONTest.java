package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class RequestJSONTest {
    @Autowired
    private JacksonTester<ItemRequestDto> json;
    @Autowired
    private JacksonTester<ItemRequestCreateDto> jsonCreate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @SneakyThrows
    void testRequestDto() {
        ItemRequestDto itemRequestDto = new ItemRequestDto(
                1,
                "test description",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), null);

        JsonContent<ItemRequestDto> result = json.write(itemRequestDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("test description");
        assertThat(result).extractingJsonPathStringValue("$.created").asString().isEqualTo(itemRequestDto.getCreated().toString());
        assertThat(result).extractingJsonPathStringValue("$.items").isEqualTo(null);
    }

    @Test
    @SneakyThrows
    void testRequestCreateDto() {
        ItemRequestCreateDto requestCreateDto = new ItemRequestCreateDto(
                "test description",
                1,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        JsonContent<ItemRequestCreateDto> result = jsonCreate.write(requestCreateDto);

        assertThat(result).extractingJsonPathNumberValue("$.requesterId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("test description");
        assertThat(result).extractingJsonPathStringValue("$.created").asString().isEqualTo(requestCreateDto.getCreated().toString());
    }
}
