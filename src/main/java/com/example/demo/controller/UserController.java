package com.example.demo.controller;
import com.example.demo.dto.UserUpdateDTO;
import com.example.demo.entity.User;
import com.example.demo.entity.Workout;
import com.example.demo.service.RecommendationService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/users") // All links here start with /api/users
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private RecommendationService recommendationService;

    @PostMapping // This handles SAVING data
    public User saveUser(@RequestBody User user) {
        return userService.createNewUser(user);
    }

    @GetMapping // This handles GETTING data
    public List<User> getUsers() {
        return userService.findAll();
    }
    @PostMapping("/{userId}/workouts")
    public Workout createWorkoutForUser(@PathVariable Integer userId, @RequestBody Workout workout) {
        return userService.addWorkoutToUser(userId, workout);
    }
    @GetMapping("/{userId}/recommendation")
    public Map<String, String> getUserRecommendation(@PathVariable Integer userId) {
        return recommendationService.generatePlan(userId);
    }
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Integer id, @RequestBody UserUpdateDTO updateData) {
        // We call the service instead of the repository now
        User updatedUser = userService.updateUser(id, updateData);

        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}