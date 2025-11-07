package com.in28minutes.rest.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.in28minutes.rest.service.AIImageEditService;
import com.in28minutes.rest.util.ImageUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URL;
import java.util.Base64;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/images")
public class ImageToImageController {
    private final AIImageEditService stableDiffusionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ImageToImageController(AIImageEditService stableDiffusionService) {
        this.stableDiffusionService = stableDiffusionService;
    }

    @PostMapping(value = "/img2img", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> transformImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("prompt") String prompt
    ) throws Exception {
        // Resize and encode to base64 PNG (no data:image/png;base64 prefix)
        String base64Image = ImageUtils.resizeAndConvertToBase64PNG(file.getInputStream(), 1500, 1500);

        // Send to Modelslab API
        String initialResponse = stableDiffusionService.transformImageByBase64(base64Image, prompt);
        JsonNode root = objectMapper.readTree(initialResponse);

        String status = root.path("status").asText();
        JsonNode outputs = root.path("output");

        if (!outputs.isArray() || outputs.size() == 0) {
            throw new RuntimeException("No output returned by ModelsLab.");
        }

        String base64FileUrl = outputs.get(0).asText();

        // Poll if status is "processing"
        if ("processing".equalsIgnoreCase(status)) {
            for (int i = 0; i < 20; i++) {
                Thread.sleep(10000); // wait 10s before polling

                // Just re-fetch the same .base64 file - no need to call a ModelsLab API endpoint!
                try (InputStream stream = new URL(base64FileUrl).openStream()) {
                    String base64Content = new String(stream.readAllBytes());

                    // Check if it's still processing (could be HTML error page or empty)
                    if (base64Content.length() < 100) {
                        continue; // probably not ready yet
                    }

                    byte[] imageBytes = Base64.getDecoder().decode(base64Content);
                    return ResponseEntity.ok()
                            .contentType(MediaType.IMAGE_PNG)
                            .body(imageBytes);
                } catch (Exception e) {
                    // Could log or ignore - file might not be ready yet
                    continue;
                }
            }

            throw new RuntimeException("Image processing timed out.");
        }

        // If status is "success", just download and return immediately
        if ("success".equalsIgnoreCase(status)) {
            try (InputStream stream = new URL(base64FileUrl).openStream()) {
                String base64Content = new String(stream.readAllBytes());
                byte[] imageBytes = Base64.getDecoder().decode(base64Content);

                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .body(imageBytes);
            }
        }

        throw new RuntimeException("Unexpected response from ModelsLab.");
    }


    @PostMapping(value = "/img2video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> transformImageToVideo(
            @RequestParam("prompt") String prompt
    ) throws Exception {
        String initialResponse = "";
        try {
            initialResponse = stableDiffusionService.transformImageToVideo(prompt);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        JsonNode root = objectMapper.readTree(initialResponse);

        if ("processing".equalsIgnoreCase(root.path("status").asText())) {
            String fetchUrl = root.path("fetch_result").asText();

            for (int i = 0; i < 20; i++) {
                Thread.sleep(10000); // wait 10s between polls
                String fetchResponse = stableDiffusionService.fetchProcessedVideo(fetchUrl);
                JsonNode fetchRoot = objectMapper.readTree(fetchResponse);

                JsonNode videoLinks = fetchRoot.path("proxy_links");
                if (videoLinks.isArray() && videoLinks.size() > 0) {
                    String videoUrl = videoLinks.get(0).asText();

                    byte[] videoBytes = new URL(videoUrl).openStream().readAllBytes();

                    return ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .header("Content-Disposition", "attachment; filename=\"result.mp4\"")
                            .body(videoBytes);
                }
            }

            throw new RuntimeException("Video processing timed out.");
        }

        throw new RuntimeException("Unexpected API response.");
    }
}
