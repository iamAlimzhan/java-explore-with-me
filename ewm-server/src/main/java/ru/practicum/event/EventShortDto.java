package ru.practicum.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.category.CategoryDto;
import ru.practicum.enums.EventSortEnum;
import ru.practicum.user.UserShortDto;

import java.util.Comparator;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class EventShortDto {
    Long id;
    String annotation;
    CategoryDto category;
    Integer confirmedRequests;
    String eventDate;
    UserShortDto initiator;
    Boolean paid;
    String title;
    Integer views;

    public static Comparator<EventShortDto> getComparator(EventSortEnum sortType) {
        return (sortType != null && sortType == EventSortEnum.VIEWS) ?
                Comparator.comparing(EventShortDto::getViews).reversed() :
                Comparator.comparing(EventShortDto::getEventDate).reversed();
    }
}
