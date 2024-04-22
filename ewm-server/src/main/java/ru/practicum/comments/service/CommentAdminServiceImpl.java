package ru.practicum.comments.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.comments.*;
import ru.practicum.exception.NotFoundException;

import java.util.Optional;

@Service
@AllArgsConstructor
public class CommentAdminServiceImpl implements CommentAdminService {
    private final CommentRepository commentRepository;

    @Override
    public CommentDto adminGetByCommentId(Long comId) {
        Optional<Comment> commentOptional = commentRepository.findById(comId);
        if (commentOptional.isPresent()) {
            Comment comment = commentOptional.get();
            return CommentMapper.toCommentDto(comment);
        }
        throw new NotFoundException("Комментарий не найден");
    }

    @Override
    public CommentDto adminUpdateComment(CommentCreateDto commentCreateDto, Long comId) {
        Comment existingComment = commentRepository.getOne(comId);
        existingComment.setText(commentCreateDto.getText());
        return CommentMapper.toCommentDto(commentRepository.save(existingComment));
    }

    @Override
    public void adminDeleteComment(Long comId) {
        Comment comment = commentRepository.findById(comId).orElseThrow(() -> new NotFoundException("Комментарий не найден"));
        commentRepository.deleteById(comment.getId());
    }
}
