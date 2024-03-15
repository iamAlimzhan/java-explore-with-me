package ru.practicum.events.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.categories.mapper.CategoryMapper;
import ru.practicum.events.dto.*;
import ru.practicum.events.model.Event;
import ru.practicum.locations.mapper.LocationMapper;
import ru.practicum.users.mapper.UserMapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, UserMapper.class, LocationMapper.class})
public interface EventMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventDate", source = " eventDate")
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    Event toEvent(NewEventDto newEventDto);

    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    @Mapping(target = "createdOn", expression = "java(ru.practicum.ExploreDateTimeFormatter.localDateTimeToString(event.getEventDate()))")
    @Mapping(target = "eventDate", expression = "java(ru.practicum.ExploreDateTimeFormatter.localDateTimeToString(event.getEventDate()))")
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "publishedOn", expression = "java(ru.practicum.ExploreDateTimeFormatter.localDateTimeToString(event.getPublishedOn()))")
    @Mapping(target = "views", source = "views")
    EventFullDto toEventFullDto(Event event);

    @Mapping(target = "category", source = "category")
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "eventDate", expression = "java(ru.practicum.ExploreDateTimeFormatter.localDateTimeToString(event.getEventDate()))")
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "views", ignore = true)
    EventShortDto toEventShortDto(Event event);

    @Mapping(target = "rangeStart", expression = "java(start)")
    @Mapping(target = "rangeEnd", expression = "java(end)")
    EventParamsFilt toEventFilterParams(EventParamsFiltDto filterDto, LocalDateTime start, LocalDateTime end);

    List<EventShortDto> toEventShortDtoListForEvents(List<Event> events);
}
