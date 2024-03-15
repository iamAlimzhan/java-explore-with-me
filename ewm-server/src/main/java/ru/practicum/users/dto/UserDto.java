package ru.practicum.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserDto {
    private final Long id;
    @NotBlank
    private final String name;
    @NotBlank
    @Email
    private final String email;
}
