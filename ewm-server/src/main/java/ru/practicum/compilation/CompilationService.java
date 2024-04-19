package ru.practicum.compilation;

import java.util.List;

public interface CompilationService {
    CompilationDto create(NewCompilationDto newCompilationDto);

    CompilationDto getById(Long compId);

    List<CompilationDto> getList(Boolean pinned, Integer from, Integer size);

    CompilationDto update(Long compId, UpdateCompilationRequest updateCompRequest);

    void delete(Long compId);
}
