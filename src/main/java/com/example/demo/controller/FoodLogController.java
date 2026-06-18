package com.example.demo.controller;

import com.example.demo.entity.FoodLog;
import com.example.demo.entity.User;
import com.example.demo.repository.FoodLogRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class FoodLogController {

    private final FoodLogRepository foodLogRepository;
    private final UserRepository userRepository;

    public FoodLogController(FoodLogRepository foodLogRepository, UserRepository userRepository) {
        this.foodLogRepository = foodLogRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/{userId}/food")
    public ResponseEntity<FoodLog> addFoodLog(@PathVariable Integer userId, @RequestBody FoodLog request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        request.setUser(user);
        if (request.getDateLogged() == null) {
            request.setDateLogged(LocalDateTime.now());
        }

        FoodLog savedLog = foodLogRepository.save(request);
        return ResponseEntity.ok(savedLog);
    }

    @GetMapping("/{userId}/food")
    public ResponseEntity<List<FoodLog>> getFoodLogs(@PathVariable Integer userId, @RequestParam(required = false, defaultValue = "daily") String period) {
        LocalDateTime start;
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        if ("weekly".equalsIgnoreCase(period)) {
            start = LocalDate.now().minusDays(6).atStartOfDay(); // Last 7 days including today
        } else if ("monthly".equalsIgnoreCase(period)) {
            start = LocalDate.now().minusDays(29).atStartOfDay(); // Last 30 days including today
        } else {
            start = LocalDate.now().atStartOfDay(); // Today
        }
        
        List<FoodLog> logs = foodLogRepository.findByUserIdAndDateLoggedBetween(userId, start, endOfDay);
        return ResponseEntity.ok(logs);
    }

    @DeleteMapping("/{userId}/food/{id}")
    public ResponseEntity<Void> deleteFoodLog(@PathVariable Integer userId, @PathVariable Long id) {
        FoodLog log = foodLogRepository.findById(id).orElseThrow(() -> new RuntimeException("Log not found"));
        
        if (!log.getUser().getUserId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        
        foodLogRepository.delete(log);
        return ResponseEntity.ok().build();
    }
}
