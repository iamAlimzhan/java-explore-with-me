package ru.practicum.compilation;

import org.springframework.stereotype.Component;
import ru.practicum.event.Event;
import ru.practicum.event.EventMapper;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CompilationMapper {
    public static Compilation toCompilation(NewCompilationDto newCompDto, Set<Event> events) {
        return Compilation.builder()
                .events(events)
                .pinned(newCompDto.getPinned())
                .title(newCompDto.getTitle())
                .build();
    }

    public static CompilationDto toCompilationDto(Compilation compilation) {
        return CompilationDto.builder()
                .events(compilation.getEvents().stream()
                        .map(EventMapper::toEventShortDto)
                        .collect(Collectors.toList()))
                .id(compilation.getId())
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }
}
