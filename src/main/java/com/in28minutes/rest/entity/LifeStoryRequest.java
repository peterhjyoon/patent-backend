package com.in28minutes.rest.entity;

import lombok.Data;

import java.util.List;

@Data
public class LifeStoryRequest {
    private List<EventData> events;
    private String style;

    // Getters and setters
    @Data
    public static class EventData {
        private String title;
        private String name;
        private String description;
        private String eventDate;
        private String imagePath;
        // Getters and setters
    }
}
