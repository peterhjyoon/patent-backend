package com.in28minutes.rest.repository;

import com.in28minutes.rest.entity.LifeStory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LifeStoryRepository extends JpaRepository<LifeStory, Long> {
    LifeStory findTopByOrderByIdDesc();
    void deleteById(Long id);
}
