package ru.practicum.locations.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.locations.dto.LocationDto;
import ru.practicum.locations.model.Location;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    @Mapping(target = "id", ignore = true)
    Location toLocation(LocationDto locationDto);
}
