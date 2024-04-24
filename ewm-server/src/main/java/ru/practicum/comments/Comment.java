package ru.practicum.comments;

import lombok.*;
import ru.practicum.event.Event;
import ru.practicum.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event eventId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User userId;

    @Column(name = "text")
    private String text;

    @Column(name = "created")
    private LocalDateTime dateOfPost;
}
