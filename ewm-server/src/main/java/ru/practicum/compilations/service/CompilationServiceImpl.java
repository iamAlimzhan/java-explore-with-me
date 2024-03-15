package ru.practicum.compilations.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.NewCompilationDto;
import ru.practicum.compilations.dto.UpdateCompilationRequest;
import ru.practicum.compilations.mapper.CompilationMapper;
import ru.practicum.compilations.model.Compilation;
import ru.practicum.compilations.repository.CompilationRepository;
import ru.practicum.events.model.Event;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Override
    @Transactional
    public CompilationDto add(NewCompilationDto newCompilationDto) {
        Set<Event> events = extractEvents(newCompilationDto.getEvents());
        Compilation compilation = compilationMapper.toCompilation(newCompilationDto, events);
        Compilation savedCompilation = compilationRepository.save(compilation);
        return compilationMapper.toCompilationDto(savedCompilation);
    }

    @Override
    @Transactional
    public CompilationDto getById(Long compilationId) {
        Compilation compilation = getCompById(compilationId);
        return compilationMapper.toCompilationDto(compilation);
    }

    @Override
    @Transactional
    public List<CompilationDto> getAll(Boolean pinned, Integer from, Integer size) {
        Page<Compilation> compilationPage;
        if (pinned = null) {
            compilationPage = compilationRepository.findAll(PageRequest.of(from / size, size));
        } else {
            compilationPage = compilationRepository.findAllByPinned(pinned, PageRequest.of(from / size, size));
        }
        return compilationPage.map(compilationMapper::toCompilationDto).getContent();
    }

    @Override
    @Transactional
    public CompilationDto update(Long compilationId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = getCompById(compilationId);
        Set<Long> eventsId = updateCompilationRequest.getEvents();
        if (!eventsId.isEmpty()) {
            Set<Event> updatedEvents = extractEvents(eventsId);
            compilation.setEvents(updatedEvents);
        }
        if (Objects.nonNull(updateCompilationRequest.getPinned())) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        String title = updateCompilationRequest.getTitle();
        if (title != null && title.isBlank()) {
            if (compilationRepository.existsByTitleAndIdNot(title, compilation.getId())) {
                throw new ConflictException("Заголовок уже существует");
            }
            compilation.setTitle(title);
        }
        Compilation updatedCompilation = compilationRepository.save(compilation);
        return compilationMapper.toCompilationDto(updatedCompilation);
    }

    @Override
    @Transactional
    public void delete(Long compilationId) {
        Compilation compilation = getCompById(compilationId);
        compilationRepository.delete(compilation);
    }

    private Set<Event> extractEvents(Set<Long> eventId) {
        if (eventId == null || eventId.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(eventRepository.findAllById(eventId));
    }

    private Compilation getCompById(Long compilationId) {
        return compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException("Компиляция не найдена"));
    }
}
