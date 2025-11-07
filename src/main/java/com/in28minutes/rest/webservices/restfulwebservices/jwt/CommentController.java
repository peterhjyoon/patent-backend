package com.in28minutes.rest.webservices.restfulwebservices.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", exposedHeaders = "Authorization")
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private EventRepository eventRepository;

    @PostMapping("/event/{eventId}")
    public ResponseEntity<Comment> commentOnEvent(@PathVariable Long eventId, @RequestBody Comment comment) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        comment.setEvent(event);
        return ResponseEntity.ok(commentRepository.save(comment));
    }

    @PostMapping("/life-story")
    public ResponseEntity<Comment> commentOnLifeStory(@RequestBody Comment comment) {
        comment.setLifeStoryComment(true);
        return ResponseEntity.ok(commentRepository.save(comment));
    }

    @GetMapping("/event/{eventId}")
    public List<Comment> getCommentsForEvent(@PathVariable Long eventId) {
        return commentRepository.findByEventId(eventId);
    }

    @GetMapping("/life-story")
    public List<Comment> getLifeStoryComments() {
        return commentRepository.findByLifeStoryCommentTrue();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Comment> updateComment(@PathVariable Long id, @RequestBody Comment updatedComment) {
        return commentRepository.findById(id)
                .map(existing -> {
                    existing.setContent(updatedComment.getContent());
                    return ResponseEntity.ok(commentRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        if (commentRepository.existsById(id)) {
            commentRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
