package ru.practicum.ewm.event.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.user.dto.UserShortDto;
import ru.practicum.ewm.user.model.User;

/** Converts between Event entity and DTOs. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventMapper {

    /** Creates an Event entity from DTO.*/
    public static Event toEntity(NewEventDto dto, Category category, User initiator) {
        return Event.builder()
                .annotation(dto.getAnnotation())
                .description(dto.getDescription())
                .title(dto.getTitle())
                .category(category)
                .initiator(initiator)
                .location(toLocation(dto.getLocation()))
                .eventDate(dto.getEventDate())
                .paid(dto.isPaid())
                .participantLimit(dto.getParticipantLimit())
                .requestModeration(dto.isRequestModeration())
                .confirmedRequests(0)
                .build();
    }

    /** Converts Event entity to short DTO with provided views. */
    public static EventShortDto toShortDto(Event e, long views) {
        return toShortDto(e, views, 0L);
    }

    /** Converts Event entity to short DTO with provided views and commentCount. */
    public static EventShortDto toShortDto(Event e, long views, long commentCount) { // CHANGE
        return EventShortDto.builder()
                .id(e.getId())
                .annotation(e.getAnnotation())
                .title(e.getTitle())
                .category(toCategoryDto(e.getCategory()))
                .initiator(toUserShortDto(e.getInitiator()))
                .paid(e.isPaid())
                .eventDate(e.getEventDate())
                .confirmedRequests(e.getConfirmedRequests())
                .views(views)
                .commentCount(commentCount)
                .build();
    }

    /** Converts Event entity to full DTO with provided views. */
    public static EventFullDto toFullDto(Event e, long views) {
        return toFullDto(e, views, 0L);
    }

    /** Converts Event entity to full DTO with provided views and commentCount. */
    public static EventFullDto toFullDto(Event e, long views, long commentCount) { // CHANGE
        return EventFullDto.builder()
                .id(e.getId())
                .annotation(e.getAnnotation())
                .description(e.getDescription())
                .title(e.getTitle())
                .category(toCategoryDto(e.getCategory()))
                .initiator(toUserShortDto(e.getInitiator()))
                .location(toLocationDto(e.getLocation()))
                .paid(e.isPaid())
                .participantLimit(e.getParticipantLimit())
                .requestModeration(e.isRequestModeration())
                .state(e.getState() != null ? e.getState().name() : null)
                .eventDate(e.getEventDate())
                .createdOn(e.getCreatedOn())
                .publishedOn(e.getPublishedOn())
                .confirmedRequests(e.getConfirmedRequests())
                .views(views)
                .commentCount(commentCount)
                .build();
    }

    /** Applies partial update from initiator request (nulls are ignored). */
    public static void applyUserPatch(Event e, UpdateEventUserRequest dto, Category newCategoryOrNull) {
        if (dto.getAnnotation() != null) e.setAnnotation(dto.getAnnotation());
        if (dto.getDescription() != null) e.setDescription(dto.getDescription());
        if (dto.getTitle() != null) e.setTitle(dto.getTitle());

        if (dto.getCategory() != null && newCategoryOrNull != null) {
            e.setCategory(newCategoryOrNull);
        }

        if (dto.getLocation() != null) {
            if (e.getLocation() == null) e.setLocation(new Location());
            e.getLocation().setLat(dto.getLocation().getLat());
            e.getLocation().setLon(dto.getLocation().getLon());
        }

        if (dto.getPaid() != null) e.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) e.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) e.setRequestModeration(dto.getRequestModeration());
        if (dto.getEventDate() != null) e.setEventDate(dto.getEventDate());
    }

    /** Applies partial update from admin request (nulls are ignored). */
    public static void applyAdminPatch(Event e, UpdateEventAdminRequest dto, Category newCategoryOrNull) {
        if (dto.getAnnotation() != null) e.setAnnotation(dto.getAnnotation());
        if (dto.getDescription() != null) e.setDescription(dto.getDescription());
        if (dto.getTitle() != null) e.setTitle(dto.getTitle());

        if (dto.getCategory() != null && newCategoryOrNull != null) {
            e.setCategory(newCategoryOrNull);
        }

        if (dto.getLocation() != null) {
            if (e.getLocation() == null) e.setLocation(new Location());
            e.getLocation().setLat(dto.getLocation().getLat());
            e.getLocation().setLon(dto.getLocation().getLon());
        }

        if (dto.getPaid() != null) e.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) e.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) e.setRequestModeration(dto.getRequestModeration());
        if (dto.getEventDate() != null) e.setEventDate(dto.getEventDate());
    }

    // ---- helpers ----

    private static CategoryDto toCategoryDto(Category c) {
        return (c == null) ? null
                : CategoryDto.builder()
                .id(c.getId())
                .name(c.getName())
                .build();
    }

    private static UserShortDto toUserShortDto(User u) {
        return (u == null) ? null
                : UserShortDto.builder()
                .id(u.getId())
                .name(u.getName())
                .build();
    }

    private static Location toLocation(LocationDto d) {
        return (d == null) ? null
                : Location.builder()
                .lat(d.getLat())
                .lon(d.getLon())
                .build();
    }

    private static LocationDto toLocationDto(Location l) {
        return (l == null) ? null
                : LocationDto.builder()
                .lat(l.getLat())
                .lon(l.getLon())
                .build();
    }
}