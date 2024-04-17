package ru.practicum.category;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService{
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    @Override
    public CategoryDto create(NewCategoryDto newCatId) {
        Category category = CategoryMapper.toCategory(newCatId);
        Category createCategory = categoryRepository.save(category);
        return CategoryMapper.toCategoryDto(createCategory);
    }

    @Override
    public CategoryDto getById(Long catId) {
        Category category = checkIfExists(catId);
        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    public List<CategoryDto> getAllList(Integer from, Integer size) {
        List<Category> categoryList = categoryRepository.findAll(PageRequest.of(from, size)).getContent();
        return categoryList.stream().map(CategoryMapper::toCategoryDto).collect(Collectors.toList());
    }

    @Override
    public CategoryDto update(CategoryDto catDto, Long catId) {
        Category category = checkIfExists(catId);
        String categoryName = catDto.getName();
        String currentName = category.getName();
        category.setName(StringUtils.defaultIfBlank(categoryName, currentName));
        CategoryDto categoryDto = CategoryMapper.toCategoryDto(categoryRepository.save(category));
        return categoryDto;
    }

    @Override
    public void delete(Long catId) {
        Category category = checkIfExists(catId);
        Event event = eventRepository.findAllByCategoryId(catId);
        if (event != null) {
            throw new ConflictException("Категория не удалена");
        }
        categoryRepository.delete(category);
    }
    private Category checkIfExists(long catId) {
        Optional<Category> categoryOptional = categoryRepository.findById(catId);
        if (categoryOptional.isPresent()) {
            return categoryOptional.get();
        } else {
            throw new NotFoundException("Категория не найдена");
        }
    }
}