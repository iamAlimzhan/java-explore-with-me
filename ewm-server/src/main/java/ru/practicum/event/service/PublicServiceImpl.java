package ru.practicum.event.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.DateTimeFormat;
import ru.practicum.Stats;
import ru.practicum.StatsClient;
import ru.practicum.enums.EventSortEnum;
import ru.practicum.enums.RequestStatus;
import ru.practicum.enums.StateEvent;
import ru.practicum.event.*;
import ru.practicum.event.EventFiltre;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ErrorRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.RequestRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicServiceImpl implements PublicService{
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    @PersistenceContext
    private EntityManager entityManager;
    private final StatsClient statsClient;
    private final EventRepository repository;
    private final ObjectMapper objectMapper;
    private String app = "ewm-server";

    @Override
    public List<EventShortDto> getListByPublic(EventsParamsFiltre paramsFiltre, HttpServletRequest request) {
        EventFiltre params = reformFiltredParams(paramsFiltre);
        List<Event> events = findEventsListByPublic(params);
        String requestURI = request.getRequestURI();
        String remoteAdd = request.getRemoteAddr();
        LocalDateTime time = LocalDateTime.now();
        statsClient.postHits(app, requestURI, remoteAdd, time);
        return events.stream()
                .map(EventMapper::toEventShortDto)
                .sorted(EventShortDto.getComparator(params.getSort()))
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventDtoByPublic(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Собыите не найдено"));
        if (event.getState() != StateEvent.PUBLISHED) {
            throw new NotFoundException("Собыите не найдено");
        }
        EventFullDto eventFullDto = EventMapper.toEventFullDto(event);
        eventFullDto.setViews(Math.toIntExact(fetchAndCalculateEventViews(eventFullDto.getId())));
        eventFullDto.setConfirmedRequests(Math.toIntExact(requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED)));

        statsClient.postHits(app, request.getRequestURI(), request.getRemoteAddr(), LocalDateTime.now());

        return eventFullDto;
    }

    public List<Event> findEventsListByPublic(EventFiltre params) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> criteriaQuery = criteriaBuilder.createQuery(Event.class);
        Root<Event> eventRoot = criteriaQuery.from(Event.class);
        Predicate criteria = criteriaBuilder.conjunction();
        LocalDateTime rangeStart = (params.getRangeStart() != null) ? params.getRangeStart() : LocalDateTime.now();
        criteriaBuilder.and(criteria, eventRoot.get("state").in((params.getStates() != null && !params.getStates().isEmpty()) ? params.getStates() : List.of(StateEvent.PUBLISHED)));
        if (params.getText() != null && !params.getText().isEmpty()) {
            String searchValue = ("%" + params.getText() + "%").toLowerCase();
            Predicate annotation = criteriaBuilder.like(criteriaBuilder.lower(eventRoot.get("annotation")), searchValue);
            Predicate description = criteriaBuilder.like(criteriaBuilder.lower(eventRoot.get("description")), searchValue);
            criteriaBuilder.and(criteria, criteriaBuilder.or(annotation, description));
        }
        if (params.getCategories() != null && !params.getCategories().isEmpty()) {
            criteriaBuilder.and(criteria, eventRoot.get("category").in(params.getCategories()));
        }
        if (params.getPaid() != null) {
            criteriaBuilder.and(criteria, criteriaBuilder.equal(eventRoot.get("paid"), params.getPaid()));
        }
        if (params.getRangeStart() != null) {
            criteriaBuilder.and(criteria, criteriaBuilder.greaterThanOrEqualTo(eventRoot.get("eventDate"), rangeStart));
        }
        if (params.getRangeEnd() != null) {
            criteriaBuilder.and(criteria, criteriaBuilder.lessThanOrEqualTo(eventRoot.get("eventDate"), params.getRangeEnd()));
        }
        criteriaQuery.select(eventRoot).where(criteria);
        return entityManager.createQuery(criteriaQuery)
                .setFirstResult(params.getFrom())
                .setMaxResults(params.getSize())
                .getResultList();
    }

    private EventFiltre reformFiltredParams(EventsParamsFiltre paramsDto) {
        EventFiltre params;
        String startString = paramsDto.getRangeStart();
        String endString = paramsDto.getRangeEnd();
        LocalDateTime start = (startString != null) ?
                DateTimeFormat.stringToTime(URLDecoder.decode(startString, StandardCharsets.UTF_8)) :
                LocalDateTime.now();
        LocalDateTime end = (endString != null) ?
                DateTimeFormat.stringToTime(URLDecoder.decode(endString, StandardCharsets.UTF_8)) :
                null;
        if (end != null && end.isBefore(start)) {
            throw new ErrorRequestException("Неверное временное ограницение параметров");
        }
        params = EventMapper.toEventsParamsFiltre(paramsDto, start, end);
        return params;
    }

    public Long fetchAndCalculateEventViews(Long eventId) {
        Event event = repository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        if (event.getState() != StateEvent.PUBLISHED) {
            return 0L;
        }
        LocalDateTime start = LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.now();
        String uri = "/events/" + event.getId();
        ResponseEntity<Object> response = statsClient.getStats(start, end, List.of(uri), true);
        Object responseBody = response.getBody();
        if (responseBody == null) {
            return 0L;
        }
        try {
            String responseValue = objectMapper.writeValueAsString(responseBody);
            List<Stats> viewStats = objectMapper.readValue(responseValue, new TypeReference<>() {
            });
            return viewStats.isEmpty() ? 0 : viewStats.get(0).getHits();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка при обработке JSON", e);
        }
    }
}
