package com.in28minutes.rest.webservices.restfulwebservices.jwt;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles;

    @ManyToMany(mappedBy = "members")
    private Set<Group> groups = new HashSet<>();

    public User() {}

    public User(String username, String password, List<String> roles, Set<Group> groups) {
        this.username = username;
        this.password = password;
        this.roles = roles;
        this.groups = groups;
    }

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.groups = groups; }

    public Set<Group> getGroups() { return groups; }
    public void setGroups(Set<Group> groups) { this.groups = groups; }
}
