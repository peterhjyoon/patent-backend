package com.in28minutes.rest.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class AIImageEditService {

    private static final String API_URL = "https://modelslab.com/api/v3/img2img";
    private static final String API_KEY = "STDdK5FhpUGMN9iESeKyYeNk1IOTLkOpG3g2TFcEQumo8JCu1vOgTudLTSlG"; // Replace with your API key

    private final WebClient webClient;

    public AIImageEditService(WebClient.Builder builder) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(60));

        this.webClient = builder
                .baseUrl("https://modelslab.com")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(5 * 1024 * 1024)) // 5MB
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
//        this.webClient = builder.baseUrl("https://modelslab.com")
//                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                .build();
    }

    public String transformImageByBase64(String base64Image, String prompt) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("key", API_KEY);
        payload.put("prompt", prompt);
        payload.put("negative_prompt", null);
        payload.put("init_image", base64Image);
        payload.put("width", "512");
        payload.put("model_id", "flux-kontext-dev");
        payload.put("height", "512");
        payload.put("samples", "1");
        payload.put("num_inference_steps", "30");
        payload.put("safety_checker", "no");
        payload.put("enhance_prompt", "yes");
        payload.put("guidance_scale", 7.5);
        payload.put("strength", 0.7);
        payload.put("seed", null);
        payload.put("base64", "yes"); // ✅ Must match the image format

        return webClient.post()
                .uri("/api/v6/images/img2img")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

//    public String fetchProcessedImage(String fetchUrl) {
//        Map<String, Object> payload = new HashMap<>();
//        payload.put("key", API_KEY); // ✅ Include API key
//        payload.put("model_id", "flux-kontext-dev");
//
//        return webClient.post()
//                .uri(fetchUrl.replace("https://modelslab.com", ""))
//                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                .bodyValue(payload)
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();
//    }

    public String fetchProcessedImage(String fetchUrl) {
        return webClient.get()
                .uri(fetchUrl.replace("https://modelslab.com", ""))
                .header("key", API_KEY)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String fetchProcessedVideo(String fetchUrl) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("key", API_KEY); // ✅ Include API key
        payload.put("model_id", "svd");

        return webClient.post()
                .uri(fetchUrl.replace("https://modelslab.com", "")) // convert full URL to
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(payload)// relative path
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String transformImageToVideo(String prompt) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("key", API_KEY);
        payload.put("output_type", "mp4");
        payload.put("model_id", "svd");
        payload.put("prompt", prompt);
        payload.put("base64", "yes");
        payload.put("num_frames", 81);
        payload.put("clip_skip", 2);
        payload.put("upscale_height", 1024);
        payload.put("upscale_width", 1024);
        payload.put("upscale_strength", 0.7);
        payload.put("upscale_num_inference_steps", 40);
        payload.put("upscale_guidance_scale", 6.5);
        payload.put("motion_bucket_id", 20);
        payload.put("improved_sampling_seed", 42); // optional
        payload.put("negative_prompt", "blurry, distorted");
        payload.put("fps", 16);

        return webClient.post()
                .uri("/api/v6/video/text2video_ultra")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("key", API_KEY)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}