package ru.practicum.comments.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.comments.*;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CommentPrivateServiceImpl implements CommentPrivateService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public CommentDto createComment(CommentCreateDto commentCreateDto, Long userId, Long eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        Optional<User> user = userRepository.findById(userId);
        if (event.isEmpty()) {
            throw new NotFoundException("Событие не найдено");
        }
        if (user.isEmpty()) {
            throw new NotFoundException("Пользователь не найден");
        }
        Comment comment = new Comment();
        comment.setText(commentCreateDto.getText());
        comment.setUserId(user.get());
        comment.setEventId(event.get());
        comment.setDateOfPost(LocalDateTime.now());
        Comment createComment = commentRepository.save(comment);
        return CommentMapper.toCommentDto(createComment);
    }

    @Override
    public CommentDto getById(Long userId, Long comId) {
        if (!commentRepository.existsById(comId)) {
            throw new NotFoundException("Комментарий не найден");
        }
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        Comment comment = commentRepository.findById(comId).get();
        if (!comment.getUserId().getId().equals(userId)) {
            throw new ConflictException("Не удается получить комментарий, созданный другим пользователем");
        }
        return CommentMapper.toCommentDto(comment);
    }

    @Override
    public List<CommentDto> getCommentListByDateTime(Long userId, LocalDateTime start,
                                                     LocalDateTime end, Integer from, Integer size) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new NotFoundException("Пользователь не найден");
        }
        User user = userOptional.get();
        Pageable pageable = PageRequest.of(from, size, Sort.by("dateOfPost").ascending());
        if (start != null && end != null) {
            return CommentMapper.toCommentDtoList(commentRepository.findByUserAndDateOfPostBetween(user, start, end, pageable));
        } else if (start != null) {
            return CommentMapper.toCommentDtoList(commentRepository.findByUserAndDateOfPostAfter(user, start, pageable));
        } else if (end != null) {
            return CommentMapper.toCommentDtoList(commentRepository.findByUserAndDateOfPostBefore(user, end, pageable));
        } else {
            return CommentMapper.toCommentDtoList(commentRepository.findByUser(user, pageable));
        }
    }

    @Override
    public CommentDto updateComment(CommentCreateDto commentCreateDto, Long userId, Long comId) {
        Optional<Comment> commentOptional = commentRepository.findById(comId);
        if (!commentOptional.isPresent()) {
            throw new NotFoundException("Комментарий не найден");
        }
        Comment comment = commentOptional.get();
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        if (!comment.getUserId().getId().equals(userId)) {
            throw new ConflictException("Комментарий не обновлен! Пользователь не является автором поста");
        }
        comment.setText(commentCreateDto.getText());
        Comment updatedComment = commentRepository.save(comment);
        return CommentMapper.toCommentDto(updatedComment);
    }

    @Override
    public void deleteComment(Long userId, Long comId) {
        Optional<Comment> commentOptional = commentRepository.findById(comId);
        Optional<User> userOptional = userRepository.findById(userId);
        if (commentOptional.isPresent() && userOptional.isPresent()) {
            Comment comment = commentOptional.get();
            if (comment.getUserId().getId().equals(userId)) {
                commentRepository.delete(comment);
            } else {
                throw new ConflictException("Комментарий не удален! Пользователь не является автором поста");
            }
        } else {
            if (!commentOptional.isPresent()) {
                throw new NotFoundException("Комментарий не найден");
            } else {
                throw new NotFoundException("Пользователь не найден");
            }
        }
    }
}