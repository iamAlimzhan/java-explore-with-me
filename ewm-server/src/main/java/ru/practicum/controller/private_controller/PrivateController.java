package ru.practicum.controller.private_controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comments.CommentCreateDto;
import ru.practicum.comments.CommentDto;
import ru.practicum.comments.service.CommentPrivateService;
import ru.practicum.event.EventFullDto;
import ru.practicum.event.EventShortDto;
import ru.practicum.event.NewEventDto;
import ru.practicum.event.UpdateEventUserRequest;
import ru.practicum.event.service.EventPrivateService;
import ru.practicum.request.EventRequestStatusUpdateRequest;
import ru.practicum.request.EventRequestStatusUpdateResult;
import ru.practicum.request.ParticipationRequestDto;
import ru.practicum.request.RequestService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
@Validated
public class PrivateController {
    private static final String PATTERN_DATE = ("yyyy-MM-dd HH:mm:ss");
    private final EventPrivateService eventService;
    private final RequestService requestService;
    private final CommentPrivateService commentPrivateService;

    @GetMapping(value = "/{userId}/events")
    public List<EventShortDto> get(@PathVariable Long userId,
                                   @RequestParam(defaultValue = "0") Integer from,
                                   @RequestParam(defaultValue = "10") Integer size) {
        return eventService.getPrivateList(userId, from, size);
    }

    @GetMapping(value = "/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getRequests(@PathVariable Long userId,
                                                     @PathVariable Long eventId) {
        return eventService.getPrivateRequestList(userId, eventId);
    }

    @GetMapping(value = "/{userId}/events/{eventId}")
    public EventFullDto getEvent(@PathVariable Long userId,
                                 @PathVariable Long eventId) {
        return eventService.getPrivateEventDto(userId, eventId);
    }

    @PatchMapping(value = "/{userId}/events/{eventId}")
    public EventFullDto update(@PathVariable Long userId,
                               @PathVariable Long eventId,
                               @Valid @RequestBody UpdateEventUserRequest updateRequest) {
        return eventService.updatePrivateByEventDto(updateRequest, userId, eventId);
    }

    @PatchMapping(value = "/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult update(@PathVariable Long userId,
                                                 @PathVariable Long eventId,
                                                 @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        return eventService.updatePrivateRequestStatus(updateRequest, userId, eventId);
    }

    @PostMapping(value = "/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto add(@PathVariable Long userId,
                            @Valid @RequestBody NewEventDto newEventDto) {
        return eventService.createPrivateByEventDto(newEventDto, userId);
    }

    @GetMapping(value = "/{userId}/requests")
    public List<ParticipationRequestDto> get(@PathVariable Long userId) {
        return requestService.getList(userId);
    }

    @PostMapping(value = "/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto add(@PathVariable Long userId,
                                       @RequestParam Long eventId) {
        return requestService.create(userId, eventId);
    }

    @PatchMapping(value = "/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancel(@PathVariable Long userId,
                                          @PathVariable Long requestId) {
        return requestService.delete(userId, requestId);
    }

    //------------------------ Фича комментарии---------------
    @PostMapping("/{userId}/comments/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@RequestBody @Valid CommentCreateDto commentCreateDto,
                                    @PathVariable Long userId,
                                    @PathVariable Long eventId) {
        return commentPrivateService.createComment(commentCreateDto, userId, eventId);
    }

    @PatchMapping("/{userId}/comments/{commentId}")
    public CommentDto updateComment(@RequestBody @Valid CommentCreateDto commentCreateDto,
                                    @PathVariable Long userId,
                                    @PathVariable Long commentId) {
        return commentPrivateService.updateComment(commentCreateDto, userId, commentId);
    }

    @GetMapping("/{userId}/comments/{commentId}")
    public CommentDto getByCommentId(@PathVariable Long userId,
                                     @PathVariable Long commentId) {
        return commentPrivateService.getById(userId, commentId);
    }

    @GetMapping("/{userId}/comments")
    public List<CommentDto> getCommentListByDateTime(@PathVariable(value = "userId") Long userId,
                                                     @RequestParam(required = false)
                                                     @DateTimeFormat(pattern = PATTERN_DATE)
                                                     LocalDateTime start,
                                                     @RequestParam(required = false)
                                                     @DateTimeFormat(pattern = PATTERN_DATE)
                                                     LocalDateTime end,
                                                     @RequestParam(defaultValue = "0") Integer from,
                                                     @RequestParam(defaultValue = "10") Integer size) {
        return commentPrivateService.getCommentListByDateTime(userId, start, end, from, size);
    }

    @DeleteMapping("/{userId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId,
                              @PathVariable Long commentId) {
        commentPrivateService.deleteComment(userId, commentId);
    }
}
