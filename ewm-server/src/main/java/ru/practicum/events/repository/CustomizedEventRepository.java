package ru.practicum.events.repository;

import ru.practicum.events.dto.EventParamsFilt;
import ru.practicum.events.model.Event;

import java.util.List;

public interface CustomizedEventRepository {
    List<Event> publicSearch(EventParamsFilt params);

    List<Event> adminSearch(EventParamsFilt params);
}
