package com.example.demo.controller;

import com.example.demo.dto.ExerciseLogRequest;
import com.example.demo.entity.ExerciseLog;
import com.example.demo.entity.User;
import com.example.demo.repository.ExerciseLogRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class ExerciseController {

    private final ExerciseLogRepository exerciseLogRepository;
    private final UserRepository userRepository;

    public ExerciseController(ExerciseLogRepository exerciseLogRepository, UserRepository userRepository) {
        this.exerciseLogRepository = exerciseLogRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/{userId}/exercises")
    public ResponseEntity<ExerciseLog> addExerciseLog(@PathVariable Integer userId, @RequestBody ExerciseLogRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        ExerciseLog log = new ExerciseLog();
        log.setExerciseName(request.getExerciseName());
        log.setWeight(request.getWeight());
        log.setSets(request.getSets());
        log.setReps(request.getReps());
        log.setUser(user);

        ExerciseLog savedLog = exerciseLogRepository.save(log);
        return ResponseEntity.ok(savedLog);
    }

    @GetMapping("/{userId}/exercises")
    public ResponseEntity<List<ExerciseLog>> getExerciseLogs(@PathVariable Integer userId) {
        List<ExerciseLog> logs = exerciseLogRepository.findByUserUserId(userId);
        return ResponseEntity.ok(logs);
    }
}