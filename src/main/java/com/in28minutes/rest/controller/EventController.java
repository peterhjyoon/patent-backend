package com.in28minutes.rest.controller;

import com.in28minutes.rest.entity.Comment;
import com.in28minutes.rest.entity.Event;
import com.in28minutes.rest.repository.EventRepository;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:4200", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class EventController {

    private final EventRepository repo;
    private final OpenAiChatModel chatModel; // <-- Inject AI model

    public EventController(EventRepository repo, OpenAiChatModel chatModel) {
        this.repo = repo;
        this.chatModel = chatModel;
    }

    @GetMapping
    @CrossOrigin(origins = "http://localhost:4200")
    public List<Event> getEvents() {
        return repo.findAllByOrderByTimestampDesc();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @CrossOrigin(origins = "http://localhost:4200")
    public Event createEvent(
            @RequestParam("title") String title,
            @RequestParam(value = "name", required = false) String name,   // <-- add this
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("eventDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate eventDate,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            @RequestParam(value = "video", required = false) MultipartFile videoFile
    ) throws IOException {
        Event event = new Event();
        event.setTitle(title);
        event.setName(name);   // <-- set it
        event.setDescription(description);
        event.setEventDate(eventDate);

        if (imageFile != null && !imageFile.isEmpty()) {
            String filename = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
            Path path = Paths.get("uploads", filename);
            Files.createDirectories(path.getParent());
            Files.copy(imageFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            event.setImagePath(filename);
        }

        if (videoFile != null && !videoFile.isEmpty()) {
            String filename = UUID.randomUUID() + "_" + videoFile.getOriginalFilename();
            Path path = Paths.get("uploads", filename);
            Files.createDirectories(path.getParent());
            Files.copy(videoFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            event.setVideoPath(filename);
        }

        return repo.save(event);
    }

    @GetMapping("/images/{filename}")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) throws IOException {
        Path path = Paths.get("uploads", filename);
        Resource file = new UrlResource(path.toUri());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    @GetMapping("/videos/{filename}")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<Resource> getVideo(@PathVariable String filename) throws IOException {
        Path path = Paths.get("uploads", filename);
        Resource file = new UrlResource(path.toUri());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                .contentType(MediaType.valueOf("video/mp4")) // adjust if multiple types
                .body(file);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @CrossOrigin(origins = "http://localhost:4200")
    public Event updateEvent(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("eventDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate eventDate,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            @RequestParam(value = "video", required = false) MultipartFile video
    ) throws IOException {
        Optional<Event> optionalEvent = repo.findById(id);
        if (optionalEvent.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        }

        Event event = optionalEvent.get();
        event.setTitle(title);
        event.setDescription(description);
        event.setEventDate(eventDate);

        if (imageFile != null && !imageFile.isEmpty()) {
            String filename = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
            Path path = Paths.get("uploads", filename);
            Files.createDirectories(path.getParent());
            Files.copy(imageFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            event.setImagePath(filename);
        }

        return repo.save(event);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build(); // 404 if not found
        }

        repo.deleteById(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @PostMapping("/resume")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> generateResume(@RequestBody List<Long> eventIds) {
        List<Event> events = repo.findAllById(eventIds);

        // Convert events into structured text for AI
        StringBuilder input = new StringBuilder("Generate a professional resume based on these life events:\n\n");
        for (Event e : events) {
            input.append("- Title: ").append(e.getTitle()).append("\n");
            if (e.getEventDate() != null) {
                input.append("  Date: ").append(e.getEventDate()).append("\n");
            }
            if (e.getDescription() != null) {
                input.append("  Description: ").append(e.getDescription()).append("\n");
            }
            input.append("\n");
        }

        // Send prompt to AI
        var response = chatModel.call(new Prompt(input.toString()));
        String resumeText = response.getResult().getOutput().getContent();

        return ResponseEntity.ok(resumeText);
    }

    @PostMapping("/{eventId}/accept-comment/{commentId}")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<Event> acceptCommentAndUpdateDescription(@PathVariable Long eventId, @PathVariable Long commentId) {
        Optional<Event> optionalEvent = repo.findById(eventId);
        if (optionalEvent.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        }

        Event event = optionalEvent.get();

        // Find the comment
        Optional<Comment> optionalComment = event.getComments().stream()
                .filter(comment -> comment.getId().equals(commentId))
                .findFirst();

        if (optionalComment.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found");
        }

        Comment comment = optionalComment.get();

        // Construct the AI prompt
        String promptText = String.format(
                "You are helping edit an event description. Here is the original description:\n\n\"%s\"\n\n" +
                        "Now please revise it to naturally incorporate this user comment:\n\n\"%s\"\n\n" +
                        "Return only the revised description.",
                event.getDescription() != null ? event.getDescription() : "",
                comment.getContent()
        );

        // Call the AI model
        var response = chatModel.call(new Prompt(promptText));
        String updatedDescription = response.getResult().getOutput().getContent();

        // Update event
        event.setDescription(updatedDescription);
        repo.save(event);

        return ResponseEntity.ok(event);
    }
}
