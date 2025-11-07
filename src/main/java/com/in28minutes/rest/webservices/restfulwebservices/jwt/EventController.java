package com.in28minutes.rest.webservices.restfulwebservices.jwt;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:4200", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE}, allowedHeaders = "*", exposedHeaders = "Authorization")
public class EventController {

    private final EventRepository repo;
    private final OpenAiChatModel chatModel; // <-- Inject AI model
    private final EventRepository eventRepository;

    public EventController(EventRepository repo, OpenAiChatModel chatModel, EventRepository eventRepository) {
        this.repo = repo;
        this.chatModel = chatModel;
        this.eventRepository = eventRepository;
    }

    @GetMapping("/{id}")
    public Event getEvent(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        Event event = eventRepository.findById(id).orElseThrow();

        if (event.getGroup() != null) {
            if (!event.getGroup().getMembers().contains(currentUser)) {
                throw new AccessDeniedException("Not in this group");
            }
        } else if (!event.getOwner().equals(currentUser)) {
            throw new AccessDeniedException("Not your event");
        }

        return event;
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
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            @RequestParam(value = "video", required = false) MultipartFile videoFile
    ) throws IOException {
        Event event = new Event();
        event.setTitle(title);
        event.setName(name);   // <-- set it
        event.setDescription(description);
        event.setStartDate(startDate);
        event.setEndDate(endDate);

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
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
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
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
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
        event.setStartDate(startDate);
        event.setEndDate(endDate);

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
            if (e.getStartDate() != null) {
                input.append("  Start Date: ").append(e.getStartDate()).append("\n");
            }
            if (e.getEndDate() != null) {
                input.append("  End Date: ").append(e.getEndDate()).append("\n");
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

    @PostMapping("/merge-videos")
    public ResponseEntity<Resource> mergeVideos(@RequestBody List<Long> eventIds) throws Exception {
        Path uploadsDir = Paths.get("uploads");
        Files.createDirectories(uploadsDir);

        List<Path> captionedVideos = new ArrayList<>();

        for (Long eventId : eventIds) {
            Event event = repo.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found: " + eventId));

            String inputVideo = event.getVideoPath();
            if (inputVideo == null) continue;

            Path inputVideoPath = uploadsDir.resolve(inputVideo);
            String caption = event.getDescription() != null ? event.getDescription() : "No Description";
            String captionedName = "captioned_" + eventId + "_" + System.currentTimeMillis() + ".mp4";
            Path captionedPath = uploadsDir.resolve(captionedName);

            // FFmpeg command to overlay text
            ProcessBuilder pb = new ProcessBuilder(
                    "C:\\Users\\John\\Downloads\\ffmpeg\\bin\\ffmpeg.exe", // adjust path
                    "-i", inputVideoPath.toAbsolutePath().toString(),
                    "-vf", "drawtext=text='" + caption.replace(":", "\\:").replace("'", "\\'") + "':fontcolor=white:fontsize=16:box=1:boxcolor=black@0.5:boxborderw=10:x=(w-text_w)/2:y=h-th-50",
                    "-codec:a", "copy",
                    captionedPath.toAbsolutePath().toString()
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[FFmpeg] " + line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("FFmpeg failed for video ID " + eventId + " with exit code " + exitCode);
            }

            captionedVideos.add(captionedPath);
        }

        // Create FFmpeg list file
        Path fileList = uploadsDir.resolve("filelist.txt");
        List<String> lines = captionedVideos.stream()
                .map(p -> "file '" + p.toAbsolutePath().toString().replace("\\", "/") + "'")
                .toList();
        Files.write(fileList, lines);

        // Output final merged video
        String mergedFileName = "merged-" + System.currentTimeMillis() + ".mp4";
        Path outputMerged = uploadsDir.resolve(mergedFileName);

        ProcessBuilder mergePb = new ProcessBuilder(
                "C:\\Users\\John\\Downloads\\ffmpeg\\bin\\ffmpeg.exe",
                "-f", "concat",
                "-safe", "0",
                "-i", fileList.toAbsolutePath().toString(),
                "-c", "copy",
                outputMerged.toAbsolutePath().toString()
        );

        mergePb.redirectErrorStream(true);
        Process mergeProcess = mergePb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(mergeProcess.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[FFmpeg-Merge] " + line);
            }
        }

        int mergeExit = mergeProcess.waitFor();
        if (mergeExit != 0 || !Files.exists(outputMerged)) {
            throw new RuntimeException("Merging failed with exit code " + mergeExit);
        }

        UrlResource resource = new UrlResource(outputMerged.toUri());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("video/mp4"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + mergedFileName + "\"")
                .body(resource);
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
