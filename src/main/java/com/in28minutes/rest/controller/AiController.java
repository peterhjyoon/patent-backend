package com.in28minutes.rest.controller;

import com.in28minutes.rest.service.ChatService;
import com.in28minutes.rest.service.ImageService;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
public class AiController {

    private final ChatService chatService;
    private final ImageService imageService;
    private final WebClient webClient;

    public AiController(ChatService chatService, ImageService imageService, WebClient.Builder webClientBuilder) {
        this.chatService = chatService;
        this.imageService = imageService;
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("generate-image")
    public ResponseEntity<Resource> generateImage(@RequestParam("prompt") String prompt) {
        try {
            // Generate the image via Spring AI
            ImageResponse imageResponse = imageService.generateImage(prompt);
            String imageUrl = imageResponse.getResult().getOutput().getUrl();
            System.out.println("Fetching image from URL: " + imageUrl);

            // 2. Use WebClient to fetch image binary
            byte[] imageBytes = webClient
                .get()
                .uri(URI.create(imageUrl)) // Use URI.create to preserve the exact URL
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    System.err.println("Failed to fetch image: " + response.statusCode());
                    return Mono.error(new RuntimeException("Failed to fetch image from Azure"));
                })
                .bodyToMono(byte[].class)
                .block(); // You can make this reactive if needed

            // 3. Prepare and return binary response
            ByteArrayResource resource = new ByteArrayResource(imageBytes);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG); // Or derive from response if needed

            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        } catch (NonTransientAiException e) {
            System.err.println("AI error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ByteArrayResource(("Prompt rejected: " + e.getMessage()).getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("ask-ai")
    public String askAi(@RequestParam("prompt") String prompt){
        return chatService.queryAi(prompt);
    }


    @GetMapping("city-guide")
    public String cityGuide(@RequestParam("city") String city, @RequestParam("interest") String interest) {
        return chatService.getCityGuide(city, interest);
    }
}
