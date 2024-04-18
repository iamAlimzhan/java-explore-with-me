package ru.practicum.compilation;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    public CompilationDto create(NewCompilationDto newCompilationDto) {
        Set<Long> eventIds = newCompilationDto.getEvents();
        Set<Event> events = (eventIds == null || eventIds.isEmpty()) ? Collections.emptySet() : new HashSet<>(eventRepository.findAllById(eventIds));
        Compilation compilation = CompilationMapper.toCompilation(newCompilationDto, events);
        Compilation createdCompilation = compilationRepository.save(compilation);
        return CompilationMapper.toCompilationDto(createdCompilation);
    }

    @Override
    public CompilationDto getById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Компиляция не найдена"));
        return CompilationMapper.toCompilationDto(compilation);
    }

    @Override
    public List<CompilationDto> getList(Boolean pinned, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        Page<Compilation> compilationsPage = compilationRepository.findByPinned(pinned, pageable);
        List<Compilation> compilations = compilationsPage.getContent();

        return compilations.stream()
                .map(CompilationMapper::toCompilationDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto update(Long compId, UpdateCompilationRequest updateCompRequest) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Компиляция не найдена"));
        ;
        updateCompilation(compilation, updateCompRequest);
        Compilation updatedCompilation = compilationRepository.save(compilation);
        return CompilationMapper.toCompilationDto(updatedCompilation);
    }

    @Override
    public void delete(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Компиляция не найдена");
        }
        compilationRepository.deleteById(compId);
    }

    private void updateCompilation(Compilation compilation, UpdateCompilationRequest updateRequest) {
        if (updateRequest.getEvents() != null && !updateRequest.getEvents().isEmpty()) {
            Set<Event> updatedEvents = updateRequest.getEvents().stream()
                    .map(eventId -> eventRepository.findById(eventId)
                            .orElseThrow(() -> new NotFoundException("Событие не найдено, id = " + eventId)))
                    .collect(Collectors.toSet());
            compilation.setEvents(updatedEvents);
        }
        Boolean pinned = updateRequest.getPinned();
        if (pinned != null) {
            compilation.setPinned(pinned);
        }
        String title = updateRequest.getTitle();
        if (title != null && !title.isBlank()) {
            if (compilationRepository.existsByTitleAndIdNot(title, compilation.getId())) {
                throw new ConflictException("Заголовок уже существует");
            }
            compilation.setTitle(title);
        }
    }
}