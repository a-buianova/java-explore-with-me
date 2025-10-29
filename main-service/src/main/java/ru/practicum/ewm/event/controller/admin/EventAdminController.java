package ru.practicum.ewm.event.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.ewm.event.dto.AdminEventSearchRequest;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.service.EventService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Validated
public class EventAdminController {

    private final EventService service;

    /** Returns events by filters (users, states, categories, date range) with offset pagination. */
    @GetMapping
    public Collection<EventFullDto> searchAdmin(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size
    ) {
        AdminEventSearchRequest req = new AdminEventSearchRequest();
        req.setUsers(users);
        req.setStates(states);
        req.setCategories(categories);
        req.setRangeStart(rangeStart);
        req.setRangeEnd(rangeEnd);
        req.setFrom(from);
        req.setSize(size);

        if (req.getRangeStart() != null && req.getRangeEnd() != null
                && req.getRangeEnd().isBefore(req.getRangeStart())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "rangeEnd must be after rangeStart");
        }

        var page = service.searchAdmin(
                req.getUsers(),
                req.getStates(),
                req.getCategories(),
                req.getRangeStart(),
                req.getRangeEnd(),
                req.getFrom(),
                req.getSize()
        );

        return page != null ? page.getContent() : List.of();
    }

    /** Publishes or rejects an event by admin action (PUBLISH_EVENT / REJECT_EVENT). */
    @PatchMapping("/{eventId}")
    public EventFullDto updateByAdmin(@PathVariable long eventId,
                                      @RequestBody @Valid UpdateEventAdminRequest dto) {
        return service.updateByAdmin(eventId, dto);
    }
}