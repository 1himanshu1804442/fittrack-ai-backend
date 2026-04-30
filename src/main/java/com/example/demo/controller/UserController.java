package com.example.demo.controller;

import com.example.demo.dto.UserUpdateDTO;
import com.example.demo.entity.User;
import com.example.demo.entity.Workout;
import com.example.demo.repository.WorkoutRepository; // NEW IMPORT
import com.example.demo.service.RecommendationService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private WorkoutRepository workoutRepository;

    @PostMapping
    public User saveUser(@RequestBody User user) {
        return userService.createNewUser(user);
    }

    @GetMapping
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