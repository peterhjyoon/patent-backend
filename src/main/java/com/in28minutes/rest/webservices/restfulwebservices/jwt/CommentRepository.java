package com.in28minutes.rest.webservices.restfulwebservices.jwt;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByEventId(Long eventId);
    List<Comment> findByLifeStoryCommentTrue();
}
