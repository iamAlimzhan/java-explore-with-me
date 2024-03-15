package ru.practicum.categories.dto;

import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;

@Getter
@Builder
public class CategoryDto {
    private final Long id;
    @NotBlank
    private final String name;
}
