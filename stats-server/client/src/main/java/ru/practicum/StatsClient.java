package ru.practicum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class StatsClient extends BaseClient {
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public StatsClient(@Value("${stats-service.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> postHits(HitsDto hitDto) {
        return post("/hit", hitDto);
    }

    public ResponseEntity<Object> getStats(String start, String end, List<String> uris, Boolean uniq) {
        Map<String, Object> parameters = Map.of(
                "start", encode(start),
                "end", encode(end),
                "uris", String.join(",", uris),
                "unique", uniq
        );
        return get("/stats?start={start}&end={end}&uris={uris}&uniq={uniq}", parameters);
    }

    private String encode(String date) {
        LocalDateTime dateTime = LocalDateTime.parse(date, DATE_TIME_FORMAT);
        return dateTime.format(DATE_TIME_FORMAT);
    }
}
