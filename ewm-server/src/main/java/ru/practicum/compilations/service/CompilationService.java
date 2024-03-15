package ru.practicum.compilations.service;

import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.NewCompilationDto;
import ru.practicum.compilations.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto add(NewCompilationDto newCompilationDto);

    CompilationDto getById(Long compilationId);

    List<CompilationDto> getAll(Boolean pinned, Integer from, Integer size);

    CompilationDto update(Long compilationId, UpdateCompilationRequest updateCompilationRequest);

    void delete(Long compilationId);

}
