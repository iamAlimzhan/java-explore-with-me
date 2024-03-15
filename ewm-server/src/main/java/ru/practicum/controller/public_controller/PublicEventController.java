package ru.practicum.controller.public_controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventParamsFiltDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.service.PublicService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/events")
public class PublicEventController {
    private final PublicService publicService;

    @GetMapping
    public List<EventShortDto> get(@Valid EventParamsFiltDto params, HttpServletRequest request) {
        return publicService.getByPublicList(params, request);
    }

    @GetMapping(value = "/{id}")
    public EventFullDto get(@PathVariable Long id, HttpServletRequest request) {
        return publicService.getByPublic(id, request);
    }
}
