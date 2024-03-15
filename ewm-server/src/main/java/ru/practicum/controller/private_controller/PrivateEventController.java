package ru.practicum.controller.private_controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.NewEventDto;
import ru.practicum.events.dto.UpdateEventUserRequest;
import ru.practicum.events.service.PrivateService;
import ru.practicum.requests.dto.EventRequestStatusUpdateRequestDto;
import ru.practicum.requests.dto.EventRequestStatusUpdateResultDto;
import ru.practicum.requests.dto.ParticipationRequestDto;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class PrivateEventController {
    private final PrivateService service;

    @GetMapping(value = "/{userId}/events")
    public List<EventShortDto> get(@PathVariable Long userId,
                                   @RequestParam(defaultValue = "0") Integer from,
                                   @RequestParam(defaultValue = "10") Integer size) {
        return service.getByPrivateList(userId, from, size);
    }

    @GetMapping(value = "/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getRequests(@PathVariable Long userId,
                                                     @PathVariable Long eventId) {
        return service.getByPrivateRequests(userId, eventId);
    }

    @GetMapping(value = "/{userId}/events/{eventId}")
    public EventFullDto getEvent(@PathVariable Long userId,
                                 @PathVariable Long eventId) {
        return service.getByPrivate(userId, eventId);
    }

    @PatchMapping(value = "/{userId}/events/{eventId}")
    public EventFullDto update(@PathVariable Long userId,
                               @PathVariable Long eventId,
                               @Valid @RequestBody UpdateEventUserRequest updateRequest) {
        return service.updateByPrivate(updateRequest, userId, eventId);
    }

    @PatchMapping(value = "/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResultDto update(@PathVariable Long userId,
                                                    @PathVariable Long eventId,
                                                    @RequestBody EventRequestStatusUpdateRequestDto updateRequest) {
        return service.updateByPrivateStatus(updateRequest, userId, eventId);
    }

    @PostMapping(value = "/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto add(@PathVariable Long userId,
                            @Valid @RequestBody NewEventDto newEventDto) {
        return service.createByPrivate(newEventDto, userId);
    }
}
