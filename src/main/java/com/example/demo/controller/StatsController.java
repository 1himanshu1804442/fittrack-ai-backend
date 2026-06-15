package com.example.demo.controller;

import com.example.demo.dto.UserStatsDTO;
import com.example.demo.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class StatsController {

    @Autowired
    private StatsService statsService;

    @GetMapping("/{userId}/stats")
    public ResponseEntity<UserStatsDTO> getUserStats(@PathVariable Integer userId) {
        return ResponseEntity.ok(statsService.getUserStats(userId));
    }
}