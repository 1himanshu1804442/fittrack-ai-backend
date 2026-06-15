package com.example.demo.service;

import com.example.demo.dto.UserStatsDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.ExerciseLogRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class StatsService {

    @Autowired
    private ExerciseLogRepository exerciseLogRepository;

    @Autowired
    private UserRepository userRepository;

    public UserStatsDTO getUserStats(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        Double volume = exerciseLogRepository.calculateTotalVolumeSince(userId, oneWeekAgo);
        int weeklyVolume = volume != null ? volume.intValue() : 0;

        int workoutStreak = 12;
        int recoveryScore = 72;
        double currentWeight = user.getBodyWeight() != null ? user.getBodyWeight().doubleValue() : 0.0;

        return new UserStatsDTO(workoutStreak, weeklyVolume, recoveryScore, currentWeight);
    }
}