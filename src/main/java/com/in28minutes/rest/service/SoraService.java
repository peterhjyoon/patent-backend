package com.in28minutes.rest.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class SoraService {

    private final WebClient webClient;
    private final String soraModelName;

    public SoraService(WebClient.Builder webClientBuilder,
                       @Value("${azure.openai.endpoint}") String endpoint,
                       @Value("${azure.openai.api-key}") String apiKey,
                       @Value("${azure.openai.sora-model-name}") String soraModelName) {
        this.webClient = webClientBuilder.baseUrl(endpoint)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("api-key", apiKey)
                .build();
        this.soraModelName = soraModelName;
    }

    public Mono<String> generateVideo(String prompt, int width, int height, int duration) {
        String requestBody = String.format(
                "{\"model\": \"%s\", \"prompt\": \"%s\", \"width\": %d, \"height\": %d, \"n_seconds\": %d}",
                soraModelName, prompt, width, height, duration
        );
        return webClient.post()
                .uri("/openai/v1/video/generations/jobs?api-version=preview")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class); // Parse response for job ID
    }

    public Mono<String> getJobStatus(String jobId) {
        return webClient.get()
                .uri("/openai/v1/video/generations/jobs/{jobId}?api-version=preview", jobId)
                .retrieve()
                .bodyToMono(String.class); // Parse response for status and video URL
    }
}
