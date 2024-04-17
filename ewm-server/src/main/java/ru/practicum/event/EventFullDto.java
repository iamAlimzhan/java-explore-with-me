package ru.practicum.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.category.CategoryDto;
import ru.practicum.enums.EventSortEnum;
import ru.practicum.enums.StateEvent;
import ru.practicum.location.LocationDto;
import ru.practicum.user.UserShortDto;

import java.util.Comparator;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class EventFullDto {
    private Long id;
    private String annotation;
    private CategoryDto category;
    private Integer confirmedRequests;
    private String createdOn;
    private String description;
    private String eventDate;
    private UserShortDto initiator;
    private LocationDto location;
    private Boolean paid;
    private Integer participantLimit;
    private String publishedOn;
    private Boolean requestModeration;
    private StateEvent state;
    private String title;
    private Integer views;

    public static Comparator<EventFullDto> getComparator(EventSortEnum sortType) {
        return (sortType != null && sortType == EventSortEnum.VIEWS) ?
                EventComparator.EVENT_FULL_VIEWS_COMPARATOR.reversed() :
                EventComparator.EVENT_FULL_DATE_COMPARATOR.reversed();
    }
}
