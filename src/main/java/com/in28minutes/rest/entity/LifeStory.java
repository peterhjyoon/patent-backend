package com.in28minutes.rest.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class LifeStory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private String story;

    private String subjectName;

    private String style;

    private LocalDateTime generatedAt = LocalDateTime.now();
}