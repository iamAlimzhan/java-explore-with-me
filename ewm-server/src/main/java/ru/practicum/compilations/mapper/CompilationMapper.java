package ru.practicum.compilations.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.NewCompilationDto;
import ru.practicum.compilations.model.Compilation;
import ru.practicum.events.mapper.EventMapper;
import ru.practicum.events.model.Event;

import java.util.Set;

@Mapper(componentModel = "spring", uses = {EventMapper.class})
public interface CompilationMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", expression = "java(events)")
    Compilation toCompilation(NewCompilationDto newCompDto, Set<Event> events);

    CompilationDto toCompilationDto(Compilation compilation);
}
