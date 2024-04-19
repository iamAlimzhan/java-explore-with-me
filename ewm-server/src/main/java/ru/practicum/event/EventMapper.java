package ru.practicum.event;

import org.springframework.stereotype.Component;
import ru.practicum.category.CategoryDto;
import ru.practicum.location.LocationDto;
import ru.practicum.user.UserShortDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class EventMapper {
    public static Event toEvent(NewEventDto eventCreateDto) {
        return Event.builder()
                .annotation(eventCreateDto.getAnnotation())
                .confirmedRequests(0)
                .description(eventCreateDto.getDescription())
                .eventDate(eventCreateDto.getEventDate())
                .paid(eventCreateDto.getPaid())
                .participantLimit(eventCreateDto.getParticipantLimit())
                .requestModeration(eventCreateDto.getRequestModeration())
                .title(eventCreateDto.getTitle())
                .views(0)
                .build();
    }

    public static EventShortDto toEventShortDto(Event event) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryDto.builder()
                        .id(event.getCategory().getId())
                        .name(event.getCategory().getName())
                        .build())
                .eventDate(ru.practicum.DateTimeFormat.timeToString(event.getEventDate()))
                .initiator(UserShortDto.builder()
                        .id(event.getInitiator().getId())
                        .name(event.getInitiator().getName())
                        .build())
                .paid(event.getPaid())
                .title(event.getTitle())
                .confirmedRequests(event.getConfirmedRequests())
                .build();
    }

    public static EventFullDto toEventFullDto(Event event) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryDto.builder()
                        .id(event.getCategory().getId())
                        .name(event.getCategory().getName())
                        .build())
                .confirmedRequests(event.getConfirmedRequests())
                .createdOn(ru.practicum.DateTimeFormat.timeToString(event.getCreatedOn()))
                .description(event.getDescription())
                .eventDate(ru.practicum.DateTimeFormat.timeToString(event.getEventDate()))
                .initiator(UserShortDto.builder()
                        .id(event.getInitiator().getId())
                        .name(event.getInitiator().getName())
                        .build())
                .location(LocationDto.builder()
                        .lat(event.getLocation().getLat())
                        .lon(event.getLocation().getLon())
                        .build())
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(ru.practicum.DateTimeFormat.timeToString(event.getPublishedOn()))
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
    }

    public static List<EventShortDto> toEventShortDtoForEventsList(List<Event> events) {
        return Objects.isNull(events) ? null :
                events.stream()
                        .map(EventMapper::toEventShortDto)
                        .collect(Collectors.toList());
    }

    public static EventFiltre toEventsParamsFiltre(EventsParamsFiltre filterDto, LocalDateTime start, LocalDateTime end) {
        if (Objects.isNull(filterDto) && Objects.isNull(start) && Objects.isNull(end)) {
            return null;
        }
        EventFiltre.EventFiltreBuilder eventParamsFilt = EventFiltre.builder();
        if (Objects.nonNull(filterDto)) {
            eventParamsFilt
                    .ids(filterDto.getIds() != null ? new ArrayList<>(filterDto.getIds()) : null)
                    .states(filterDto.getStates() != null ? new ArrayList<>(filterDto.getStates()) : null)
                    .categories(filterDto.getCategories() != null ? new ArrayList<>(filterDto.getCategories()) : null)
                    .from(filterDto.getFrom())
                    .size(filterDto.getSize())
                    .text(filterDto.getText())
                    .paid(filterDto.getPaid())
                    .onlyAvailable(filterDto.getOnlyAvailable())
                    .sort(filterDto.getSort());
        }
        return eventParamsFilt
                .rangeStart(start)
                .rangeEnd(end)
                .build();
    }
}