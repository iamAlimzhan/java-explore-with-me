package ru.practicum.requests.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.requests.dto.ParticipationRequestDto;
import ru.practicum.requests.model.ParticipationRequest;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RequestMapper {
    @Mapping(target = "event", source = "event.id")
    @Mapping(target = "created", expression = "java(ru.practicum.DateTimeFormat.localDateTimeToString(request.getCreated()))")
    @Mapping(target = "requester", source = "requester.id")
    @Mapping(target = "status", source = "status")
    ParticipationRequestDto toRequestDto(ParticipationRequest request);

    List<ParticipationRequestDto> toRequestDtoList(List<ParticipationRequest> requests);
}
