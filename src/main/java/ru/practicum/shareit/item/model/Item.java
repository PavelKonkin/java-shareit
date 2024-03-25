package ru.practicum.shareit.item.model;

import lombok.*;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.User;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Builder(toBuilder = true)
public class Item {
    private Integer id;
    private String name;
    private String description;
    private User owner;
    private Boolean available;
}
