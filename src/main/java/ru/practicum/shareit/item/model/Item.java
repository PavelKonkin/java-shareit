package ru.practicum.shareit.item.model;

import lombok.*;
import org.springframework.stereotype.Component;

/**
 * TODO Sprint add-controllers.
 */

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Builder(toBuilder = true)
public class Item {
    private Integer id;
    private String name;
    private String description;
    private Integer ownerId;
    private Boolean available;
    @Getter
    private static int idCounter = 1;

    public static void increaseIdCounter() {
        idCounter++;
    }
}
