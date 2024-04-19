package ru.practicum;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Stats {
    private String app;
    private String uri;
    private Long hits;
}
