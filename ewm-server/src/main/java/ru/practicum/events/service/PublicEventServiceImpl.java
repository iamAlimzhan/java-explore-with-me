package ru.practicum.events.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.DateTimeFormat;
import ru.practicum.events.dto.*;
import ru.practicum.events.enums.SortedEvent;
import ru.practicum.events.enums.StateEvent;
import ru.practicum.events.mapper.EventMapper;
import ru.practicum.events.model.Event;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.exception.ErrorRequestException;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.requests.enums.StatusRequest;
import ru.practicum.requests.model.ConfirmedRequest;
import ru.practicum.requests.repository.RequestRepository;
import ru.practicum.stats_service.StatService;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicEventServiceImpl implements PublicService {
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final EventMapper eventMapper;
    private final StatService statService;

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getByPublicList(EventParamsFiltDto paramsDto, HttpServletRequest request) {
        EventParamsFilt params = convertInputParams(paramsDto);
        List<Event> events = eventRepository.publicSearch(params);
        statService.getViewsList(events);
        getConfirmedRequest(events);
        statService.createHit(request);
        return events.stream().map(eventMapper::toEventShortDto)
                .sorted(getComparator(params.getSort())).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getByPublic(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found."));
        boolean published = (event.getState() == StateEvent.PUBLISHED);
        if (!published) {
            throw new NotFoundException("Event not found.");
        }
        EventFullDto eventFullDto = eventMapper.toEventFullDto(event);
        Long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), StatusRequest.CONFIRMED);
        Long eventFullDtoId = eventFullDto.getId();
        Long views = statService.getViews(eventFullDtoId);
        eventFullDto.setViews(views);
        eventFullDto.setConfirmedRequests(confirmedRequests);
        statService.createHit(request);
        return eventFullDto;
    }

    /*private Event getEventIfExists(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found."));
    }*/

    /*private EventFullDto completeEventFullDto(Event event) {
        EventFullDto eventFullDto = eventMapper.toEventFullDto(event);
        Long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), StatusRequest.CONFIRMED);
        Long eventId = eventFullDto.getId();
        Long views = statService.getViews(eventId);
        eventFullDto.setViews(views);
        eventFullDto.setConfirmedRequests(confirmedRequests);
        return eventFullDto;
    }*/

    /*private void completeWithViews(EventDto eventDto) {
        Long eventId = eventDto.getId();
        Long views = statService.getViews(eventId);
        eventDto.setViews(views);
    }*/

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
            return DateTimeFormat.stringToLocalDateTime(URLDecoder.decode(dateTimeString,
                    StandardCharsets.UTF_8));
        }
        return defaultValue;
    }

    private void getConfirmedRequest(List<Event> events) {
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        List<Object[]> confirmedRequests = requestRepository.findConfirmedRequest(eventIds);
        Map<Long, Long> confirmedRequestsMap = new HashMap<>();

        for (Object[] result : confirmedRequests) {
            Long eventId = (Long) result[0];
            Long count = (Long) result[1];
            confirmedRequestsMap.put(eventId, count);
        }

        events.forEach(event -> event.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(), 0L)));
    }


    private Comparator<EventDto> getComparator(SortedEvent eventSort) {
        return EventDto.getComparator(eventSort);
    }
}
