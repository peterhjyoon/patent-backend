package com.in28minutes.rest.webservices.restfulwebservices.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    @PostMapping
    public Group createGroup(@RequestBody Group group, @AuthenticationPrincipal User currentUser) {
        group.setOwner(currentUser);
        group.getMembers().add(currentUser);
        return groupRepository.save(group);
    }

    @PostMapping("/{groupId}/add/{userId}")
    public Group addMember(@PathVariable Long groupId, @PathVariable Long userId,
                           @AuthenticationPrincipal User currentUser) {
        Group group = groupRepository.findById(groupId).orElseThrow();
        if (!group.getOwner().equals(currentUser)) {
            throw new AccessDeniedException("Only owner can add members");
        }
        User user = userRepository.findById(userId).orElseThrow();
        group.getMembers().add(user);
        return groupRepository.save(group);
    }

    @GetMapping
    public List<Group> myGroups(@AuthenticationPrincipal User currentUser) {
        return groupRepository.findByMembersContaining(currentUser);
    }
}
