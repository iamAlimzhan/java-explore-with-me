package ru.practicum.event;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.practicum.category.Category;
import ru.practicum.enums.StateEvent;
import ru.practicum.location.Location;
import ru.practicum.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String annotation;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @JoinColumn(name = "confirmed_requests")
    private transient Integer confirmedRequests;

    @JoinColumn(name = "created_on")
    @CreationTimestamp
    private LocalDateTime createdOn;

    private String description;

    @JoinColumn(name = "event_date")
    private LocalDateTime eventDate;

    @ManyToOne
    @JoinColumn(name = "initiator_id")
    private User initiator;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    private Boolean paid;

    @JoinColumn(name = "participant_limit")
    private Integer participantLimit;

    @JoinColumn(name = "published_on")
    private LocalDateTime publishedOn;

    @JoinColumn(name = "request_moderation")
    private Boolean requestModeration;

    @Enumerated(value = EnumType.STRING)
    private StateEvent state;

    private String title;

    private transient Integer views;
}
