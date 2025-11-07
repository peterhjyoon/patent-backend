package com.in28minutes.rest.repository;

import com.in28minutes.rest.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByOrderByTimestampDesc();
}
