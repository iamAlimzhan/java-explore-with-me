package ru.practicum.categories.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.dto.NewCategoryDto;
import ru.practicum.categories.mapper.CategoryMapper;
import ru.practicum.categories.model.Category;
import ru.practicum.categories.repository.CategoryRepository;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDto add(NewCategoryDto newCategoryDto) {
        Category category = categoryMapper.toCategory(newCategoryDto);
        Category addCategory = categoryRepository.save(category);
        return categoryMapper.toCategoryDto(addCategory);
    }

    @Override
    @Transactional
    public CategoryDto getById(Long categoryId) {
        Category category = getCategoryIfExists(categoryId);
        return categoryMapper.toCategoryDto(category);
    }

    @Override
    @Transactional
    public List<CategoryDto> getAll(Integer from, Integer size) {
        Page<Category> pageCategory = categoryRepository.findAll(PageRequest.of(from, size));
        return pageCategory.map(categoryMapper::toCategoryDto).getContent();
    }

    @Override
    @Transactional
    public CategoryDto update(CategoryDto categoryDto, Long categoryId) {
        Category category = getCategoryIfExists(categoryId);
        String name = categoryDto.getName();
        String existName = category.getName();
        category.setName(StringUtils.defaultIfBlank(name, existName));
        return categoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(Long categoryId) {
        Category category = getCategoryIfExists(categoryId);
        if (eventRepository.findByCategoryId(categoryId).isPresent()) {
            throw new ConflictException("Категоря не удалилась");
        }
        categoryRepository.delete(category);
    }

    private Category getCategoryIfExists(long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));
    }
}
