package ru.practicum.service;

import lombok.AllArgsConstructor;
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
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;

    @Override
    public List<Stats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        List<Stats> statsList;
        if (uris != null) {
            if (unique) {
                statsList = statsRepository.getByDistinctIpAndTimestampAfterAndTimestampBeforeAndUriIn(start, end, uris);
            } else {
                statsList = statsRepository.getByTimestampAfterAndTimestampBeforeAndUriIn(start, end, uris);
            }
        } else {
            if (unique) {
                statsList = statsRepository.getByDistinctIpAndTimestampAfterAndTimestampBefore(start, end);
            } else {
                statsList = statsRepository.getByTimestampAfterAndTimestampBefore(start, end);
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
