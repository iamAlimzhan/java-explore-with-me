package ru.practicum.categories.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotBlank;

@Getter
@RequiredArgsConstructor
public class NewCategoryDto {
    @NotBlank
    private String name;
}
