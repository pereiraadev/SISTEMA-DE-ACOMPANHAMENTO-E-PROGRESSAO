package com.fittrack.fittrack_frontend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class ApiClient {
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final ObjectMapper mapper;
    private final String baseUrl;
    private String token;

    public ApiClient(@Value("${backend.url}") String baseUrl) {
        this.mapper = new ObjectMapper();
        this.baseUrl = baseUrl.replaceAll("/+$", "");
    }

    public synchronized void autenticar(String token) { this.token = token; }
    public synchronized void encerrarSessao() { this.token = null; }
    public synchronized boolean autenticado() { return token != null && !token.isBlank(); }

    public <T> T get(String path, Class<T> responseType) throws IOException, InterruptedException {
        return enviar("GET", path, null, responseType);
    }

    public <T> T getList(String path, TypeReference<T> responseType) throws IOException, InterruptedException {
        return enviar("GET", path, null, responseType);
    }

    public <T> T post(String path, Object body, Class<T> responseType) throws IOException, InterruptedException {
        return enviar("POST", path, body, responseType);
    }

    public <T> T put(String path, Object body, Class<T> responseType) throws IOException, InterruptedException {
        return enviar("PUT", path, body, responseType);
    }

    private <T> T enviar(String method, String path, Object body, Object responseType) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json");
        if (autenticado()) builder.header("Authorization", "Bearer " + token);
        if (body != null) {
            builder.header("Content-Type", "application/json");
            builder.method(method, HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)));
        } else builder.method(method, HttpRequest.BodyPublishers.noBody());
        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300)
            throw new ApiClientException(response.statusCode(), response.body());
        if (responseType == Void.class) return null;
        if (responseType instanceof Class<?> type) return (T) mapper.readValue(response.body(), type);
        return mapper.readValue(response.body(), (TypeReference<T>) responseType);
    }

    public static class ApiClientException extends IOException {
        private final int status;
        public ApiClientException(int status, String message) { super(message); this.status = status; }
        public int getStatus() { return status; }
    }
}
