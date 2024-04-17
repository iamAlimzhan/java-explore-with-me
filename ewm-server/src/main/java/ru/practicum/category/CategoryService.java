package ru.practicum.category;

import java.util.List;

public interface CategoryService {
    CategoryDto create(NewCategoryDto newCatId);

    CategoryDto getById(Long catId);

    List<CategoryDto> getAllList(Integer from, Integer size);

    CategoryDto update(CategoryDto catDto, Long catId);

    void delete(Long catId);
}
