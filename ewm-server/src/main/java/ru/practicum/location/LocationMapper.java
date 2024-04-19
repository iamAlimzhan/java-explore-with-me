package ru.practicum.location;

import org.springframework.stereotype.Component;

@Component
public class LocationMapper {
    public Location toLocation(LocationDto locationDto) {
        if (locationDto == null) {
            return null;
        }
        return Location.builder()
                .lat(locationDto.getLat())
                .lon(locationDto.getLon())
                .build();
    }
}

