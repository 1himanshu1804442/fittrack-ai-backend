package com.example.demo.controller;

import com.example.demo.dto.ExerciseLogRequest;
import com.example.demo.dto.OneRepMaxDataDTO;
import com.example.demo.dto.ExerciseDistributionDTO;
import com.example.demo.dto.QuickLogDataDTO;
import com.example.demo.entity.ExerciseLog;
import com.example.demo.entity.User;
import com.example.demo.repository.ExerciseLogRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.StatsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class ExerciseController {

    private final ExerciseLogRepository exerciseLogRepository;
    private final UserRepository userRepository;
    private final StatsService statsService;

    public ExerciseController(ExerciseLogRepository exerciseLogRepository, UserRepository userRepository, StatsService statsService) {
        this.exerciseLogRepository = exerciseLogRepository;
        this.userRepository = userRepository;
        this.statsService = statsService;
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
    public ResponseEntity<Page<ExerciseLog>> getExerciseLogs(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ExerciseLog> logs = exerciseLogRepository.findByUserUserIdOrderByDateLoggedDesc(userId, pageable);
        return ResponseEntity.ok(logs);
    }

    @PutMapping("/{userId}/exercises/{id}")
    public ResponseEntity<ExerciseLog> updateExerciseLog(
            @PathVariable Integer userId,
            @PathVariable Long id,
            @RequestBody ExerciseLogRequest request) {
        ExerciseLog log = exerciseLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Log not found"));
        
        if (!log.getUser().getUserId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        
        log.setExerciseName(request.getExerciseName());
        log.setWeight(request.getWeight());
        log.setSets(request.getSets());
        log.setReps(request.getReps());
        
        ExerciseLog updatedLog = exerciseLogRepository.save(log);
        return ResponseEntity.ok(updatedLog);
    }

    @DeleteMapping("/{userId}/exercises/{id}")
    public ResponseEntity<Void> deleteExerciseLog(
            @PathVariable Integer userId,
            @PathVariable Long id) {
        ExerciseLog log = exerciseLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Log not found"));
                
        if (!log.getUser().getUserId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        
        exerciseLogRepository.delete(log);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/exercises/names")
    public ResponseEntity<List<String>> getExerciseNames(@PathVariable Integer userId) {
        List<String> names = exerciseLogRepository.findDistinctExerciseNamesByUserId(userId);
        return ResponseEntity.ok(names);
    }

    @GetMapping("/{userId}/exercises/analytics")
    public ResponseEntity<List<OneRepMaxDataDTO>> getExerciseAnalytics(
            @PathVariable Integer userId,
            @RequestParam String exerciseName) {
        List<OneRepMaxDataDTO> analytics = statsService.getOneRepMaxAnalytics(userId, exerciseName);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/{userId}/exercises/quick-log-data")
    public ResponseEntity<List<QuickLogDataDTO>> getQuickLogData(@PathVariable Integer userId) {
        List<QuickLogDataDTO> quickLogData = statsService.getRecentExercisesWithWeights(userId);
        return ResponseEntity.ok(quickLogData);
    }

    @GetMapping("/{userId}/exercises/distribution")
    public ResponseEntity<List<ExerciseDistributionDTO>> getExerciseDistribution(@PathVariable Integer userId) {
        List<ExerciseDistributionDTO> distribution = statsService.getExerciseDistribution(userId);
        return ResponseEntity.ok(distribution);
    }
}