package com.example.demo.controller;

import com.example.demo.dto.UserStatsDTO;
import com.example.demo.dto.VolumeChartDataDTO;
import com.example.demo.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/users")
public class StatsController {

    @Autowired
    private StatsService statsService;

    @GetMapping("/{userId}/stats")
    public ResponseEntity<UserStatsDTO> getUserStats(@PathVariable Integer userId) {
        return ResponseEntity.ok(statsService.getUserStats(userId));
    }

    @GetMapping("/{userId}/analytics/volume")
    public ResponseEntity<List<VolumeChartDataDTO>> getVolumeAnalytics(@PathVariable Integer userId) {
        List<VolumeChartDataDTO> chartData = statsService.getVolumeChartData(userId);
        return ResponseEntity.ok(chartData);
    }
}