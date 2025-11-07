package com.in28minutes.rest.controller;

import com.in28minutes.rest.entity.LifeStory;
import com.in28minutes.rest.entity.LifeStoryRequest;
import com.in28minutes.rest.repository.LifeStoryRepository;
import lombok.Data;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/events")
public class LifeStoryController {

    @Data
    public static class LifeStoryResponse {
        private String story;
        private List<LifeStoryRequest.EventData> events;

        public LifeStoryResponse(String story, List<LifeStoryRequest.EventData> events) {
            this.story = story;
            this.events = events;
        }
    }

    private final ChatClient chatClient;
    private final LifeStoryRepository lifeStoryRepository;

    public LifeStoryController(ChatClient chatClient, LifeStoryRepository lifeStoryRepository) {
        this.chatClient = chatClient;
        this.lifeStoryRepository = lifeStoryRepository;
    }

    @PostMapping("/generate-story")
    public ResponseEntity<LifeStoryResponse> generateLifeStory(@RequestBody LifeStoryRequest request) {
        List<LifeStoryRequest.EventData> events = request.getEvents();
        String style = request.getStyle();

        events.sort(Comparator.comparing(e -> LocalDate.parse(e.getEventDate())));

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Write a factual and realistic chronological life story in a ")
                .append(style != null ? style : "neutral")
                .append(" tone, using only the information provided in the events below about ");

        String subjectName = events.stream()
                .filter(e -> e.getName() != null && !e.getName().isBlank())
                .map(LifeStoryRequest.EventData::getName)
                .findFirst()
                .orElse("an unknown individual");

        promptBuilder.append(subjectName).append(":\n\n");

        for (LifeStoryRequest.EventData event : events) {
            promptBuilder.append(String.format("On %s, %s", event.getEventDate(), event.getTitle()));
            if (event.getDescription() != null && !event.getDescription().isBlank()) {
                promptBuilder.append(": ").append(event.getDescription());
            }
            if (event.getImagePath() != null && !event.getImagePath().isBlank()) {
                promptBuilder.append(String.format(" [Image at: http://localhost:8081/api/events/images/%s]", event.getImagePath()));
            }
            promptBuilder.append(".\n");
        }

        String prompt = promptBuilder.toString();
        String story = chatClient.prompt()    // start building a prompt
                .user(prompt) // provide user message
                .call()       // execute the request
                .content();   // get the AI text

        // ✅ Delete the existing story by ID (if it exists)
        LifeStory existingStory = lifeStoryRepository.findTopByOrderByIdDesc();
        if (existingStory != null) {
            lifeStoryRepository.deleteById(existingStory.getId());
        }

        // ✅ Save the new story
        LifeStory lifeStory = new LifeStory();
        lifeStory.setStory(story);
        lifeStory.setStyle(style);
        lifeStory.setSubjectName(subjectName);
        lifeStoryRepository.save(lifeStory);

        return ResponseEntity.ok(new LifeStoryResponse(story, events));
    }

    @GetMapping("/latest-story")
    public ResponseEntity<LifeStoryResponse> getLatestLifeStory() {
        LifeStory latestStory = lifeStoryRepository.findTopByOrderByIdDesc();
        if (latestStory == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(new LifeStoryResponse(latestStory.getStory(), List.of()));
    }
}
