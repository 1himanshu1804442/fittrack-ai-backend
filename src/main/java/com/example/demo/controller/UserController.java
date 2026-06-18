package com.example.demo.controller;

import com.example.demo.dto.CustomWorkoutRequestDTO;
import com.example.demo.dto.UserUpdateDTO;
import com.example.demo.entity.User;
import com.example.demo.entity.Workout;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WorkoutRepository;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.RecommendationService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public record AuthenticationRequest(String username, String password) {}

    // Updated to send back the database ID to React
    public record AuthenticationResponse(String jwt, Integer userId) {}

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userService.createNewUser(user);
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.username(), authRequest.password())
            );
        } catch (Exception e) {
            return ResponseEntity.status(403).body("Incorrect username or password");
        }

        final UserDetails userDetails = userService.loadUserByUsername(authRequest.username());
        final String jwt = jwtUtil.generateToken(userDetails.getUsername());

        // Grab the actual user from the database to get their integer ID
        User user = userRepository.findByUsername(authRequest.username())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Return BOTH the token and the database ID to React
        return ResponseEntity.ok(new AuthenticationResponse(jwt, user.getUserId()));
    }

    @GetMapping
    public List<User> getUsers() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{userId}/workouts")
    public Workout createWorkoutForUser(@PathVariable Integer userId, @RequestBody Workout workout) {
        return userService.addWorkoutToUser(userId, workout);
    }

    @GetMapping("/{userId}/recommendation")
    public Map<String, String> getUserRecommendation(@PathVariable Integer userId) {
        return recommendationService.generatePlan(userId);
    }

    @PostMapping("/{userId}/custom-workout")
    public Map<String, String> getCustomUserRecommendation(@PathVariable Integer userId, @RequestBody CustomWorkoutRequestDTO request) {
        return recommendationService.generateCustomPlan(userId, request);
    }

    @GetMapping("/{userId}/coach")
    public Map<String, Object> getPerformanceCoachReview(@PathVariable Integer userId) {
        return recommendationService.generatePerformanceReview(userId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Integer id, @RequestBody UserUpdateDTO updateData) {
        User updatedUser = userService.updateUser(id, updateData);

        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/history")
    public List<Workout> getUserHistory(@PathVariable Integer id) {
        return workoutRepository.findUserHistory(id);
    }
}