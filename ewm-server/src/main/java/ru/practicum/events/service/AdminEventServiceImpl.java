package ru.practicum.events.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ExploreDateTimeFormatter;
import ru.practicum.categories.model.Category;
import ru.practicum.categories.repository.CategoryRepository;
import ru.practicum.enums.StateAdmin;
import ru.practicum.events.dto.*;
import ru.practicum.events.enums.SortedEvent;
import ru.practicum.events.enums.StateEvent;
import ru.practicum.events.mapper.EventMapper;
import ru.practicum.events.model.Event;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.exception.ErrorRequestException;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.locations.dto.LocationDto;
import ru.practicum.locations.mapper.LocationMapper;
import ru.practicum.locations.model.Location;
import ru.practicum.locations.repository.LocationRepository;
import ru.practicum.requests.model.VerifyRequest;
import ru.practicum.requests.repository.RequestRepository;
import ru.practicum.stats_service.StatService;

import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminEventServiceImpl implements AdminService {
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    private final StatService statService;

    @Override
    @Transactional
    public List<EventFullDto> getByAdmin(EventParamsFiltDto eventParamsFiltDto) {
        EventParamsFilt params = convertInputParams(eventParamsFiltDto);
        List<Event> events = eventRepository.adminSearch(params);
        statService.getViewsList(events);
        getConfirmedRequest(events);
        return events.stream()
                .map(eventMapper::toEventFullDto)
                .sorted(getComparator(params.getSort()))
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto updateByAdmin(UpdateEventAdminRequest request, Long eventId) {
        Event event = getEventIfExists(eventId);
        LocalDateTime actual = event.getEventDate();
        checkDateTimeIsAfterNowWithGap(actual, 1);
        LocalDateTime target = request.getEventDate();
        if (Objects.nonNull(target)) {
            checkDateTimeIsAfterNowWithGap(target, 2);
        }
        StateAdmin action = request.getStateAction();
        if (Objects.nonNull(action)) {
            if (action == StateAdmin.PUBLISH_EVENT) {
                publishEvent(request, event);
            } else {
                rejectEvent(event);
            }
        }
        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    private EventParamsFilt convertInputParams(EventParamsFiltDto paramsDto) {
        EventParamsFilt params;
        try {
            String startString = paramsDto.getRangeStart();
            String endString = paramsDto.getRangeEnd();
            LocalDateTime start = getFromStringOrSetDefault(startString, LocalDateTime.now());
            LocalDateTime end = getFromStringOrSetDefault(endString, null);
            if (end != null && end.isBefore(start)) {
                throw new ErrorRequestException("Invalid time-range filter params.");
            }
            params = eventMapper.toEventFilterParams(paramsDto, start, end);
        } catch (UnsupportedEncodingException e) {
            throw new ConflictException("Invalid search parameters.");
        }
        return params;
    }

    private LocalDateTime getFromStringOrSetDefault(String dateTimeString, LocalDateTime defaultValue)
            throws UnsupportedEncodingException {
        if (dateTimeString != null) {
            return ExploreDateTimeFormatter.stringToLocalDateTime(URLDecoder.decode(dateTimeString,
                    StandardCharsets.UTF_8));
        }
        return defaultValue;
    }

    private void getConfirmedRequest(List<Event> events) {
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        List<VerifyRequest> verifyRequests = requestRepository.findConfirmedRequest(eventIds);
        Map<Long, Long> confirmedRequestsMap = verifyRequests.stream()
                .collect(Collectors.toMap(VerifyRequest::getEventId, VerifyRequest::getCount));
        events.forEach(event -> event.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(), 0L)));
    }

    private Comparator<EventDto> getComparator(SortedEvent eventSort) {
        return EventDto.getComparator(eventSort);
    }

    private Event getEventIfExists(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found."));
    }

    private void checkDateTimeIsAfterNowWithGap(LocalDateTime value, Integer gapFromNowInHours) {
        LocalDateTime minValidDateTime = LocalDateTime.now().plusHours(gapFromNowInHours);
        if (value.isBefore(minValidDateTime)) {
            throw new ErrorRequestException("Invalid event date-time.");
        }
    }

    private void updateEventFields(Event event, UpdateEventAdminRequest request) {
        updateEventAnnotation(event, request.getAnnotation());
        updateEventCategory(event, request.getCategory());
        updateEventDescription(event, request.getDescription());
        updateEventDate(event, request.getEventDate());
        updateEventLocation(event, request.getLocation());
        updateEventPaidStatus(event, request.getPaid());
        updateEventParticipationLimit(event, request.getParticipantLimit());
        updateEventRequestModeration(event, request.getRequestModeration());
        updateEventTitle(event, request.getTitle());
    }

    private void publishEvent(UpdateEventAdminRequest request, Event event) {
        StateEvent state = event.getState();
        if (state == StateEvent.PUBLISHED) {
            throw new ConflictException("The event has already been published");
        }
        if (state == StateEvent.REJECTED) {
            throw new ConflictException("The event has already been rejected");
        }
        if (state == StateEvent.CANCELED) {
            throw new ConflictException("The event has already been canceled");
        }
        updateEventFields(event, request);
        event.setState(StateEvent.PUBLISHED);
        event.setPublishedOn(LocalDateTime.now());
    }

    private void rejectEvent(Event event) {
        StateEvent state = event.getState();
        if (state == StateEvent.PUBLISHED || state == StateEvent.CANCELED) {
            throw new ConflictException("You cannot reject a published event");
        }
        event.setState(StateEvent.CANCELED);
    }

    private void updateEventTitle(Event event, String title) {
        if (Objects.nonNull(title) && !title.isBlank()) {
            event.setTitle(title);
        }
    }

    private void updateEventRequestModeration(Event event, Boolean requestModeration) {
        if (Objects.nonNull(requestModeration)) {
            event.setRequestModeration(requestModeration);
        }
    }

    private void updateEventParticipationLimit(Event event, Long limit) {
        if (Objects.nonNull(limit)) {
            event.setParticipantLimit(limit);
        }
    }

    private void updateEventPaidStatus(Event event, Boolean paid) {
        if (Objects.nonNull(paid)) {
            event.setPaid(paid);
        }
    }

    private void updateEventLocation(Event event, LocationDto locationDto) {
        if (Objects.nonNull(locationDto)) {
            Location updatedLocation = getLocation(locationDto);
            event.setLocation(updatedLocation);
        }
    }

    private void updateEventDate(Event event, LocalDateTime eventDate) {
        if (Objects.nonNull(eventDate)) {
            checkDateTimeIsAfterNowWithGap(eventDate, 1);
            event.setEventDate(eventDate);
        }
    }

    private void updateEventDescription(Event event, String description) {
        if (Objects.nonNull(description) && !description.isBlank()) {
            event.setDescription(description);
        }
    }

    private void updateEventCategory(Event event, Long catId) {
        if (Objects.nonNull(catId)) {
            Category updated = getCategoryIfExists(catId);
            event.setCategory(updated);
        }
    }

    private void updateEventAnnotation(Event event, String annotation) {
        if (Objects.nonNull(annotation) && !annotation.isBlank()) {
            event.setAnnotation(annotation);
        }
    }

    private Location getLocation(LocationDto locationDto) {
        Location location = locationMapper.toLocation(locationDto);
        return locationRepository.getByLatAndLon(location.getLat(), location.getLon())
                .orElse(locationRepository.save(location));
    }

    private Category getCategoryIfExists(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found."));
    }
}
