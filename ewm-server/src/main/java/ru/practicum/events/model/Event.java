package ru.practicum.events.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.practicum.categories.model.Category;
import ru.practicum.events.enums.StateEvent;
import ru.practicum.locations.model.Location;
import ru.practicum.users.model.User;

import javax.persistence.*;
import javax.validation.Valid;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "events")
@Builder(toBuilder = true)
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String annotation;
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    @CreationTimestamp
    private LocalDateTime createdOn;
    private String description;
    private LocalDateTime eventDate;
    @ManyToOne
    @JoinColumn(name = "initiator_id")
    private User initiator;
    @ManyToOne
    @JoinColumn(name = "location_id")
    @Valid
    private Location location;
    private Boolean paid;
    private Long participantLimit;
    private LocalDateTime publishedOn;
    private Boolean requestModeration;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StateEvent state = StateEvent.PENDING;
    private String title;
    @Transient
    private Long views;
    @Transient
    private Long confirmedRequests;
}
