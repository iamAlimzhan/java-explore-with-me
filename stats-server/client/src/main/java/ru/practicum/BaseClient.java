package ru.practicum;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.List;
import java.util.Map;

public class BaseClient {
    protected final RestTemplate restTemplate;

    public BaseClient(String serverUrl, RestTemplateBuilder builder) {
        this.restTemplate = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .build();
    }

    private static ResponseEntity<Object> prepareStatsResponse(ResponseEntity<Object> objectResponseEntity) {
        if (objectResponseEntity.getStatusCode().is2xxSuccessful()) {
            return objectResponseEntity;
        }
        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(objectResponseEntity.getStatusCode());
        if (objectResponseEntity.hasBody()) {
            return responseBuilder.body(objectResponseEntity.getBody());
        }
        return responseBuilder.build();
    }

    protected <T> ResponseEntity<Object> makeAndSendRequest(HttpMethod method, String path,
                                                            @Nullable Map<String, Object> parameters, @Nullable T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<T> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Object> serverResponse;
        try {
            if (parameters != null) {
                serverResponse = restTemplate.exchange(path, method, requestEntity, Object.class, parameters);
            } else {
                serverResponse = restTemplate.exchange(path, method, requestEntity, Object.class);
            }
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
        return prepareStatsResponse(serverResponse);
    }
}
