package com.in28minutes.rest.webservices.restfulwebservices.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class JwtAuthenticationRestController {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;

    @PostMapping("/authenticate")
    public JwtResponse authenticate(@RequestBody AuthRequest authRequest) {
        // 1. Authenticate username + password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.username(), authRequest.password())
        );

        // 2. Build JWT claims
        Instant now = Instant.now();
        String scope = authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600)) // expires in 1 hour
                .subject(authentication.getName())
                .claim("roles", scope)
                .build();

        // 3. Encode JWT
        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        // 4. Return { "token": "..." }
        return new JwtResponse(token);
    }

    public record AuthRequest(String username, String password) {}
    public record JwtResponse(String token) {}
}
