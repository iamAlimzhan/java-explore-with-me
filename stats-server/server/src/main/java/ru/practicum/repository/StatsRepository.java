package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.Hits;
import ru.practicum.model.Stats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Hits, Long> {
    @Query(value = "SELECT new ru.practicum.model.Stats(h.app, h.uri, COUNT(DISTINCT h.ip)) " +
            "FROM Hits h " +
            "WHERE h.timestamp BETWEEN ?1 AND ?2 AND h.uri IN ?3 " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<Stats> getByDistinctIpAndTimestampAfterAndTimestampBeforeAndUriIn(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query(value = "SELECT NEW ru.practicum.model.Stats(h.app, h.uri, COUNT(h.ip)) " +
            "FROM Hits h " +
            "WHERE h.timestamp BETWEEN ?1 AND ?2 AND h.uri IN ?3 " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h.ip) DESC")
    List<Stats> getByTimestampAfterAndTimestampBeforeAndUriIn(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query(value = "SELECT NEW ru.practicum.model.Stats(h.app, h.uri, COUNT(DISTINCT h.ip)) " +
            "FROM Hits h " +
            "WHERE h.timestamp BETWEEN ?1 AND ?2 " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<Stats> getByDistinctIpAndTimestampAfterAndTimestampBefore(LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT NEW ru.practicum.model.Stats(h.app, h.uri, COUNT(h.ip)) " +
            "FROM Hits h " +
            "WHERE h.timestamp BETWEEN ?1 AND ?2 " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h.ip) DESC")
    List<Stats> getByTimestampAfterAndTimestampBefore(LocalDateTime start, LocalDateTime end);
}
