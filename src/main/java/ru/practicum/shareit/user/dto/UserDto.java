package ru.practicum.shareit.user.dto;

import lombok.*;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Builder(toBuilder = true)
public class UserDto {
    private String name;
    @Email
    @NotEmpty
    private String email;
    private Integer id;
}
