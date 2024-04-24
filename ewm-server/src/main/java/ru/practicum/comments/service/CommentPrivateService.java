package ru.practicum.comments.service;

import ru.practicum.comments.CommentCreateDto;
import ru.practicum.comments.CommentDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentPrivateService {
    CommentDto createComment(CommentCreateDto commentCreateDto, Long userId, Long eventId);

    CommentDto getById(Long userId, Long comId);

    List<CommentDto> getCommentListByDateTime(Long userId, LocalDateTime start, LocalDateTime end, Integer from, Integer size);

    CommentDto updateComment(CommentCreateDto commentCreateDto, Long userId, Long comId);

    void deleteComment(Long userId, Long comId);
}
