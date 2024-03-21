package ru.practicum.events.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.practicum.users.enums.StateUser;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventUserRequest extends UpdateEvent {
    private StateUser stateAction;
}
