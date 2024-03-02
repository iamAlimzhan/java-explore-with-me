package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.HitsDto;
import ru.practicum.model.Hits;

@UtilityClass
public class HitsMapper {
    public Hits toHit(HitsDto hitDto) {
        return Hits.builder()
                .app(hitDto.getApp())
                .uri(hitDto.getUri())
                .ip(hitDto.getIp())
                .timestamp(hitDto.getTimestamp())
                .build();
    }
}
