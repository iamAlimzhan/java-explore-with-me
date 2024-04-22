package ru.practicum.comments;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.user.User;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c WHERE c.userId = ?1 AND c.dateOfPost BETWEEN ?2 AND ?3")
    List<Comment> findByUserAndDateOfPostBetween(User user, LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.userId = ?1 AND c.dateOfPost > ?2")
    List<Comment> findByUserAndDateOfPostAfter(User user, LocalDateTime start, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.userId = ?1 AND c.dateOfPost < ?2")
    List<Comment> findByUserAndDateOfPostBefore(User user, LocalDateTime end, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.userId = ?1")
    List<Comment> findByUser(User user, Pageable pageable);
}
