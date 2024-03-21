package ru.practicum.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.HitsDto;
import ru.practicum.mapper.HitsMapper;
import ru.practicum.model.Stats;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;

    @Override
    public List<Stats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        List<Stats> statsList;
        if (uris != null) {
            if (unique) {
                statsList = statsRepository.getByDistinctIpAndTimestampAfterAndTimestampBeforeAndUriIn(start, end, uris);
                log.info("если уникальный и не null statsList = {}", statsList);
            } else {
                statsList = statsRepository.getByTimestampAfterAndTimestampBeforeAndUriIn(start, end, uris);
                log.info("если не уникальный и не null statsList = {}", statsList);
            }
        } else {
            if (unique) {
                statsList = statsRepository.getByDistinctIpAndTimestampAfterAndTimestampBefore(start, end);
                log.info("если уникальный но null statsList = {}", statsList);
            } else {
                statsList = statsRepository.getByTimestampAfterAndTimestampBefore(start, end);
                log.info("если не  уникальный но null statsList = {}", statsList);
            }
        }
        return statsList;
    }

    @Override
    @Transactional
    public void save(HitsDto hitDto) {
        statsRepository.save(HitsMapper.toHit(hitDto));
    }
}
