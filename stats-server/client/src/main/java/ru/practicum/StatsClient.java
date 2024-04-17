package ru.practicum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
        HitUrl url = new HitUrl(app, uri, ip, encode(timestamp));
        String hitPath = "/hit";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("app", app);
        parameters.put("uri", uri);
        parameters.put("ip", ip);
        parameters.put("timestamp", encode(timestamp));
        makeAndSendRequest(HttpMethod.POST, hitPath, parameters, url);
    }

    public ResponseEntity<Object> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start == null || end == null || end.isBefore(start)) {
            throw new ErrorRequestException("Start и end не должны быть нулевыми, а end должен быть после start");
        }
        StringBuilder pathBuilder = new StringBuilder("/stats?start=").append(encode(start))
                .append("&end=").append(encode(end));
        if (uris != null && !uris.isEmpty()) {
            uris.forEach(uri -> pathBuilder.append("&uris=").append(uri));
        }
        if (unique != null) {
            pathBuilder.append("&unique=").append(unique);
        }
        return makeAndSendRequest(HttpMethod.GET, pathBuilder.toString(), new HashMap<>(), null);
    }

    private String encode(LocalDateTime dateTime) {
        String format = dateTime.format(DATE_TIME_FORMAT);
        return format;
    }
}
