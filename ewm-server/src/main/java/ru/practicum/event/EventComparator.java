package ru.practicum.event;

import java.util.Comparator;

public class EventComparator {
    public static final Comparator<EventFullDto> EVENT_FULL_DATE_COMPARATOR = new Comparator<EventFullDto>() {
        public int compare(EventFullDto eventFullDto1, EventFullDto eventFullDto2) {
            int dateComparison = eventFullDto1.getEventDate().compareTo(eventFullDto2.getEventDate());
            if (dateComparison != 0) {
                return dateComparison;
            }
            return eventFullDto1.getId().compareTo(eventFullDto2.getId());
        }
    };

    public static final Comparator<EventFullDto> EVENT_FULL_VIEWS_COMPARATOR = new Comparator<EventFullDto>() {
        public int compare(EventFullDto eventFullDto1, EventFullDto eventFullDto2) {
            int viewsComparison = Integer.compare(eventFullDto1.getViews(), eventFullDto2.getViews());
            if (viewsComparison != 0) {
                return viewsComparison;
            }
            return eventFullDto1.getId().compareTo(eventFullDto2.getId());
        }
    };

    public static final Comparator<EventShortDto> EVENT_SHORT_DATE_COMPARATOR = new Comparator<EventShortDto>() {
        public int compare(EventShortDto eventShortDto1, EventShortDto eventShortDto2) {
            int dateComparison = eventShortDto1.getEventDate().compareTo(eventShortDto2.getEventDate());
            if (dateComparison != 0) {
                return dateComparison;
            }
            return eventShortDto1.getId().compareTo(eventShortDto2.getId());
        }
    };

    public static final Comparator<EventShortDto> EVENT_SHORT_VIEWS_COMPARATOR = new Comparator<EventShortDto>() {
        public int compare(EventShortDto eventShortDto1, EventShortDto eventShortDto2) {
            int viewsComparison = Integer.compare(eventShortDto1.getViews(), eventShortDto2.getViews());
            if (viewsComparison != 0) {
                return viewsComparison;
            }
            return eventShortDto1.getId().compareTo(eventShortDto2.getId());
        }
    };
}
