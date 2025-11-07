package com.in28minutes.rest.controller;

import com.in28minutes.rest.service.SoraService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Mono;

@Controller
public class VideoGenerationController {

    private final SoraService soraService;

    public VideoGenerationController(SoraService soraService) {
        this.soraService = soraService;
    }

    @GetMapping("/")
    public String index() {
        return "index"; // Thymeleaf template for the UI
    }

    @GetMapping("/generate")
    @ResponseBody
    public Mono<String> generateVideo(@RequestParam String prompt,
                                      @RequestParam int width,
                                      @RequestParam int height,
                                      @RequestParam int duration) {
        return soraService.generateVideo(prompt, width, height, duration);
    }

    @GetMapping("/status")
    @ResponseBody
    public Mono<String> getVideoStatus(@RequestParam String jobId) {
        return soraService.getJobStatus(jobId);
    }
}
