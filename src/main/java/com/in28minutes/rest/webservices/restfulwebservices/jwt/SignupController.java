package com.in28minutes.rest.webservices.restfulwebservices.jwt;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api")
public class SignupController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    public SignupController(UserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            JwtTokenUtil jwtTokenUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Username is already taken!"));
        }

        User user = new User(
                request.username(),
                passwordEncoder.encode(request.password()),
                List.of("ROLE_USER"),
                null
        );

        userRepository.save(user);
        userRepository.flush();

        // Generate JWT token after saving user
        String token = jwtTokenUtil.generateToken(user.getUsername());

        return ResponseEntity.ok(Map.of(
                "message", "User registered successfully!",
                "token", token
        ));
    }
}
