package com.in28minutes.rest.webservices.restfulwebservices.jwt;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByMembersContaining(User user);
}
