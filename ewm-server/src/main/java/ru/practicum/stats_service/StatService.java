package ru.practicum.stats_service;

import ru.practicum.events.model.Event;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface StatService {
    void createHit(HttpServletRequest request);

    Long getViews(Long eventId);

    void getViewsList(List<Event> events);
}
