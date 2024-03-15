package ru.practicum.categories.service;

import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto add(NewCategoryDto newCategoryDto);

    CategoryDto getById(Long categoryId);

    List<CategoryDto> getAll(Integer from, Integer size);

    CategoryDto update(CategoryDto categoryDto, Long categoryId);

    void delete(Long categoryId);
}
