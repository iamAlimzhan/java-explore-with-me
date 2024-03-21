package ru.practicum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.exception.ErrorRequestException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class StatsClient extends BaseClient {
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(serverUrl, builder);
    }

    public void postHits(String app, String uri, String ip, LocalDateTime timestamp) {
        HitUrl endpointHit = new HitUrl(app, uri, ip, encode(timestamp));
        makeAndSendRequest(HttpMethod.POST, "/hit", null, endpointHit);
    }

    public ResponseEntity<Object> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        Map<String, Object> parameters = new HashMap<>();
        if (Objects.isNull(start) || Objects.isNull(end) || end.isBefore(start)) {
            throw new ErrorRequestException("Start and end shouldn't be null, and end should be after start.");
        }
        parameters.put("start", encode(start));
        parameters.put("end", encode(end));
        StringJoiner pathBuilder = new StringJoiner("&", "/stats?start={start}&end={end}", "");
        if (Objects.nonNull(uris) && !uris.isEmpty()) {
            uris.forEach(uri -> pathBuilder.add("&uris=" + uri));
        }
        if (Objects.nonNull(unique)) {
            pathBuilder.add("&unique=" + unique);
        }
        String path = pathBuilder.toString();
        return makeAndSendRequest(HttpMethod.GET, path, parameters, null);
    }

    private String encode(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMAT);
    }
}
