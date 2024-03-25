package ru.practicum.shareit.user;

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
public class User {
    private String name;
    private String email;
    private Integer id;
}
