package ru.practicum.service;

import ru.practicum.HitsDto;
import ru.practicum.model.Stats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    List<Stats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);

    void save(HitsDto hitDto);
}
