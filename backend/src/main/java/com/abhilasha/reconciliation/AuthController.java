package com.abhilasha.reconciliation;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // Register a new user
    @PostMapping("/register")
    public Map<String, String> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (userRepository.findByUsername(username).isPresent()) {
            return Map.of("error", "Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        // Hash the password before saving - never store plain text
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        return Map.of("message", "User registered successfully");
    }

    // Log in and get a token
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            return Map.of("error", "Invalid username or password");
        }

        User user = userOpt.get();

        // Check the given password against the stored hash
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Map.of("error", "Invalid username or password");
        }

        // Credentials are correct - generate a token
        String token = jwtUtil.generateToken(username);
        return Map.of("token", token);
    }
}