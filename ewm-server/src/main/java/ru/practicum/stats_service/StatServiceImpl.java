package ru.practicum.stats_service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.Stats;
import ru.practicum.StatsClient;
import ru.practicum.events.enums.StateEvent;
import ru.practicum.events.model.Event;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.exceptions.NotFoundException;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {
    private static final TypeReference<List<Stats>> TYPE_REFERENCE_LIST = new TypeReference<>() {
    };
    private final StatsClient statsClient;
    private final EventRepository repository;
    private final ObjectMapper objectMapper;
    private String app = "ewm-server";

    @Override
    public void createHit(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();
        final LocalDateTime START_EPOCH = LocalDateTime.of(2000, 1, 1, 0, 0);
        statsClient.postHits(app, uri, ip, START_EPOCH);
    }

    @Override
    public Long getViews(Long eventId) {
        Event event = repository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found."));
        if (event.getState() != StateEvent.PUBLISHED) {
            return 0L;
        }
        //LocalDateTime start = event.getPublishedOn();
        LocalDateTime start = LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.now();
        String uri = "/events/" + event.getId();
        ResponseEntity<Object> response = statsClient.getStats(start, end, List.of(uri), true);
        try {
            String responseValue = objectMapper.writeValueAsString(response.getBody());
            List<Stats> viewStats = Arrays.asList(objectMapper.readValue(responseValue, new TypeReference<>() {
            }));
            return viewStats.isEmpty() ? 0 : viewStats.get(0).getHits();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getViewsList(List<Event> events) {
        List<String> uriList = new ArrayList<>();
        LocalDateTime start = events.get(0).getCreatedOn();
        LocalDateTime end = LocalDateTime.now();
        String uri;
        Map<String, Event> uriMap = new HashMap<>();
        for (Event event : events) {
            if (start.isBefore(event.getCreatedOn())) {
                start = event.getCreatedOn();
            }
            uri = "/events/" + event.getId();
            uriList.add(uri);
            uriMap.put(uri, event);
            event.setViews(0L);
        }
        ResponseEntity<Object> response = statsClient.getStats(start, end, uriList, true);
        if (response.getStatusCode() == HttpStatus.OK) {
            TypeReference<List<Stats>> referese = new TypeReference<>() {
            };
            List<Stats> stats = objectMapper.convertValue(response.getBody(), referese);
            stats.forEach((stat) ->
                    uriMap.get(stat.getUri()).setViews(stat.getHits()));
        }
    }
}
