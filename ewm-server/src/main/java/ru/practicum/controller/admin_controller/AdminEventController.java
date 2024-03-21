package ru.practicum.controller.admin_controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventParamsFiltDto;
import ru.practicum.events.dto.UpdateEventAdminRequest;
import ru.practicum.events.service.AdminService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/events")
@Slf4j
public class AdminEventController {
    private final AdminService service;

    @GetMapping
    public List<EventFullDto> getByAdmin(@Valid EventParamsFiltDto params) {
        log.info("получение по админу params: {}", params);
        return service.getByAdmin(params);
    }

    @PatchMapping(value = "/{eventId}")
    public EventFullDto updateByAdmin(@PathVariable Long eventId,
                                      @Valid @RequestBody UpdateEventAdminRequest request) {
        return service.updateByAdmin(request, eventId);
    }
}
