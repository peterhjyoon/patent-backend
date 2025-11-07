package com.in28minutes.rest.webservices.restfulwebservices.jwt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String title;
    @Column(length = 1000)
    private String description;
    private LocalDateTime timestamp = LocalDateTime.now(); // creation time
//    private LocalDate eventDate; // user-defined event date
    private LocalDate startDate;

    private LocalDate endDate; // Optional

    private String imagePath; // filename or full path of uploaded image
    private String videoPath;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("event") // Prevent circular serialization
    private List<Comment> comments = new ArrayList<>();

    @ManyToOne
    private User owner;

    @ManyToOne
    private Group group; // optional
}
