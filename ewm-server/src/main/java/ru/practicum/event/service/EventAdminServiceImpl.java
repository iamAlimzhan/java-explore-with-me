package ru.practicum.event.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.*;
import ru.practicum.category.Category;
import ru.practicum.category.CategoryRepository;
import ru.practicum.enums.RequestStatus;
import ru.practicum.enums.StateAction;
import ru.practicum.enums.StateEvent;
import ru.practicum.event.*;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ErrorRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.location.Location;
import ru.practicum.location.LocationMapper;
import ru.practicum.location.LocationRepository;
import ru.practicum.request.RequestRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EventAdminServiceImpl implements EventAdminService {
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;
    private final StatsClient statsClient;
    private final ObjectMapper objectMapper;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<EventFullDto> getByAdmin(EventsParamsFiltre paramsFiltre) {
        EventFiltre eventFiltre = reformFiltredParams(paramsFiltre);
        List<Event> events = findAdmin(eventFiltre);
        updateConfirmedRequests(events);
        getStatOfViews(events);
        Comparator<EventFullDto> comparator = EventFullDto.getComparator(paramsFiltre.getSort());
        return events.stream()
                .map(EventMapper::toEventFullDto)
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto updateByAdmin(UpdateEventAdminRequest adminRequest, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        LocalDateTime currentDate = event.getEventDate();
        checkDateIsAfter(currentDate, 1);
        updateEventByAdmin(adminRequest, event);
        EventFullDto eventFullDto = EventMapper.toEventFullDto(eventRepository.save(event));
        return eventFullDto;
    }

    private void updateConfirmedRequests(List<Event> eventsList) {
        eventsList.forEach(event -> {
            Long count = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
            event.setConfirmedRequests(count.intValue());
        });
    }

    private void updateEventByAdmin(UpdateEventAdminRequest adminRequest, Event event) {
        LocalDateTime date = adminRequest.getEventDate();
        if (Objects.nonNull(date)) {
            checkDateIsAfter(date, 2);
        }
        StateAction state = adminRequest.getStateAction();
        if (Objects.nonNull(state)) {
            if (state != StateAction.PUBLISH_EVENT) {
                handleCancelEvent(event);
            } else {
                handlePublishEvent(event);
            }
        }
        updateParamsEvent(event, adminRequest);
    }

    private void handlePublishEvent(Event event) {
        StateEvent state = event.getState();
        if (state == StateEvent.PUBLISHED || state == StateEvent.REJECTED || state == StateEvent.CANCELED) {
            throw new ConflictException(state.name().toLowerCase());
        }
        event.setState(StateEvent.PUBLISHED);
        event.setPublishedOn(LocalDateTime.now());
    }

    private LocalDateTime getOutOfString(String date, LocalDateTime defaultDate) {
        if (date != null) {
            try {
                LocalDateTime parsedDate = LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME);
                return parsedDate;
            } catch (DateTimeParseException e) {
                return defaultDate;
            }
        }
        return defaultDate;
    }
    private void updateParamsEvent(Event event, UpdateEventAdminRequest adminRequest) {
        if (Objects.nonNull(adminRequest.getAnnotation()) && !adminRequest.getAnnotation().isBlank()) {
            event.setAnnotation(adminRequest.getAnnotation());
        }
        if (Objects.nonNull(adminRequest.getCategory())) {
            Category updated = categoryRepository.findById(adminRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена"));
            event.setCategory(updated);
        }
        if (Objects.nonNull(adminRequest.getDescription()) && !adminRequest.getDescription().isBlank()) {
            event.setDescription(adminRequest.getDescription());
        }
        if (Objects.nonNull(adminRequest.getEventDate())) {
            checkDateIsAfter(adminRequest.getEventDate(), 1);
            event.setEventDate(adminRequest.getEventDate());
        }
        if (Objects.nonNull(adminRequest.getLocation())) {
            Location location = locationMapper.toLocation(adminRequest.getLocation());
            if (locationRepository.getByLatAndLon(location.getLat(), location.getLon()) == null) {
                locationRepository.save(location);
            }
            event.setLocation(location);
        }
        if (Objects.nonNull(adminRequest.getPaid())) {
            event.setPaid(adminRequest.getPaid());
        }
        if (Objects.nonNull(adminRequest.getParticipantLimit())) {
            event.setParticipantLimit(adminRequest.getParticipantLimit());
        }
        if (Objects.nonNull(adminRequest.getRequestModeration())) {
            event.setRequestModeration(adminRequest.getRequestModeration());
        }
        if (Objects.nonNull(adminRequest.getTitle()) && !adminRequest.getTitle().isBlank()) {
            event.setTitle(adminRequest.getTitle());
        }
    }

    public List<Event> findAdmin(EventFiltre filtreParams) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> cq = cb.createQuery(Event.class);
        Root<Event> root = cq.from(Event.class);
        Predicate predicate = cb.conjunction();
        if (!filtreParams.getIds().isEmpty()) {
            cb.and(predicate, root.get("initiator").get("id").in(filtreParams.getIds()));
        }
        if (!filtreParams.getStates().isEmpty()) {
            cb.and(predicate, root.get("state").in(filtreParams.getStates()));
        }
        if (!filtreParams.getCategories().isEmpty()) {
            cb.and(predicate, root.get("category").in(filtreParams.getCategories()));
        }
        if (filtreParams.getRangeStart() != null) {
            cb.and(predicate, cb.greaterThanOrEqualTo(root.get("eventDate"), filtreParams.getRangeStart()));
        }
        if (filtreParams.getRangeEnd() != null) {
            cb.and(predicate, cb.lessThanOrEqualTo(root.get("eventDate"), filtreParams.getRangeEnd()));
        }
        cq.select(root).where(predicate);
        return entityManager.createQuery(cq)
                .setFirstResult(filtreParams.getFrom())
                .setMaxResults(filtreParams.getSize())
                .getResultList();
    }
    public void getStatOfViews(List<Event> events) {
        List<String> uriList = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());
        LocalDateTime start = events.stream()
                .map(Event::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
        List<Stats> stats = retrieveStats(start, uriList);
        Map<String, Event> uriMap = events.stream()
                .collect(Collectors.toMap(event -> "/events/" + event.getId(), Function.identity()));
        stats.forEach(
                stat -> {
                    String uri = stat.getUri();
                    Event event = uriMap.get(uri);
                    if (event != null) {
                        event.setViews(Math.toIntExact(stat.getHits()));
                    }
                });
    }

    private List<Stats> retrieveStats(LocalDateTime start, List<String> uriList) {
        ResponseEntity<Object> response = statsClient.getStats(start, LocalDateTime.now(), uriList, true);
        if (response.getStatusCode() == HttpStatus.OK) {
            return objectMapper.convertValue(response.getBody(), new TypeReference<>() {});
        }
        return Collections.emptyList();
    }
    private void checkDateIsAfter(LocalDateTime date, Integer currentTime) {
        LocalDateTime dateTime = LocalDateTime.now().plusHours(currentTime);
        if (date.isBefore(dateTime)) {
            throw new ErrorRequestException("Неверное время события");
        }
    }
    private LocalDateTime parseDateTime(String dateTimeString, LocalDateTime defaultValue) throws UnsupportedEncodingException {
        if (dateTimeString != null) {
            LocalDateTime outOfString = getOutOfString(dateTimeString, defaultValue);
            return outOfString;
        }
        return defaultValue;
    }

    private void handleCancelEvent(Event event) {
        StateEvent state = event.getState();
        if (state == StateEvent.PUBLISHED || state == StateEvent.CANCELED) {
            throw new ConflictException("Невозможно отменить событие " + state.name().toLowerCase());
        }
        event.setState(StateEvent.CANCELED);
    }

    private EventFiltre reformFiltredParams(EventsParamsFiltre paramsFiltre) {
        try {
            LocalDateTime startDate = parseDateTime(paramsFiltre.getRangeStart(), LocalDateTime.now());
            LocalDateTime endDate = parseDateTime(paramsFiltre.getRangeEnd(), null);
            if (endDate != null && endDate.isBefore(startDate)) {
                throw new ErrorRequestException("Неверные параметры фильтра диапазона времени");
            }
            return EventMapper.toEventsParamsFiltre(paramsFiltre, startDate, endDate);
        } catch (UnsupportedEncodingException e) {
            throw new ConflictException("Неверные параметры поиска");
        }
    }
}