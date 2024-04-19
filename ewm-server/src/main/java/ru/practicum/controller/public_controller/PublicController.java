package ru.practicum.controller.public_controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.CategoryDto;
import ru.practicum.category.CategoryService;
import ru.practicum.compilation.CompilationDto;
import ru.practicum.compilation.CompilationService;
import ru.practicum.event.EventFullDto;
import ru.practicum.event.EventShortDto;
import ru.practicum.event.EventsParamsFiltre;
import ru.practicum.event.service.PublicService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping
@Slf4j
public class PublicController {
    private final PublicService publicService;
    private final CategoryService categoryService;
    private final CompilationService compilationService;

    @GetMapping("/events")
    public List<EventShortDto> get(@Valid EventsParamsFiltre params, HttpServletRequest request) {
        return publicService.getListByPublic(params, request);
    }

    @GetMapping(value = "/events/{id}")
    public EventFullDto get(@PathVariable Long id, HttpServletRequest request) {
        return publicService.getEventDtoByPublic(id, request);
    }

    @GetMapping("/categories")
    public List<CategoryDto> get(@RequestParam(defaultValue = "0") Integer from,
                                 @RequestParam(defaultValue = "10") Integer size) {
        return categoryService.getAllList(from, size);
    }

    @GetMapping(value = "/categories/{catId}")
    public CategoryDto get(@PathVariable Long catId) {
        return categoryService.getById(catId);
    }

    @GetMapping("/compilations")
    public List<CompilationDto> get(@RequestParam(required = false) Boolean pinned,
                                    @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                    @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("получение компиляции pinned: {}, from: {}, size: {}", pinned, from, size);
        return compilationService.getList(pinned, from, size);
    }

    @GetMapping(value = "/compilations/{compId}")
    public CompilationDto getById(@PathVariable Long compId) {
        return compilationService.getById(compId);
    }
}
