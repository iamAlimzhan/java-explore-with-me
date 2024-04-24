package ru.practicum.comments.service;

import ru.practicum.comments.CommentCreateDto;
import ru.practicum.comments.CommentDto;

public interface CommentAdminService {
    CommentDto adminGetByCommentId(Long comId);

    CommentDto adminUpdateComment(CommentCreateDto commentCreateDto, Long comId);

    void adminDeleteComment(Long comId);
}
