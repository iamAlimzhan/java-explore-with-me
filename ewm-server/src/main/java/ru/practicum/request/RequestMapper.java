package ru.practicum.request;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RequestMapper {
    public static List<ParticipationRequestDto> toRequestDtoList(List<Request> requests) {
        return requests.stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }
    public static ParticipationRequestDto toRequestDto(Request request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated().toString())
                .event(request.getEventId())
                .requester(request.getRequesterId())
                .status(request.getStatus().toString())
                .build();
    }
}
