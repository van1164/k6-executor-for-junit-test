package io.github.van1164.scirptBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.util.Map;

public class HttpRequest {
    @Getter
    private HttpMethod method;
    @Getter
    private String url;
    @Getter
    private Map<String, String> headers;
    private Map<String, String> body;

    private HttpRequest(Builder builder) {
        this.method = builder.method;
        this.url = builder.url;
        this.headers = builder.headers;
        this.body = builder.body;
    }

    public String getBodyAsJson() throws JsonProcessingException {
        if (body == null || body.isEmpty()) {
            return null;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(body);
    }

    public static class Builder {
        private HttpMethod method;
        private String url;
        private Map<String, String> headers;
        private Map<String, String> body;

        public Builder method(HttpMethod method) {
            this.method = method;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder body(Map<String, String> body) {
            this.body = body;
            return this;
        }

        public HttpRequest build() {
            return new HttpRequest(this);
        }
    }
}