package ru.practicum.compilations.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.practicum.events.dto.EventShortDto;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class CompilationDto {
    private final Long id;
    private final String title;
    private final Boolean pinned;
    private final List<EventShortDto> events;
}
