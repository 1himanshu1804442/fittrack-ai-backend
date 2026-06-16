package com.example.demo.service;

import com.example.demo.dto.UserStatsDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.ExerciseLogRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

        // Fetch raw timestamps and map them in Java to bypass SQL casting issues
        List<LocalDateTime> rawDates = exerciseLogRepository.findAllLogDatesByUserId(userId);
        List<LocalDate> logDates = rawDates.stream()
                .map(LocalDateTime::toLocalDate)
                .distinct()
                .collect(Collectors.toList());

        int workoutStreak = calculateStreak(logDates);
        int recoveryScore = calculateRecovery(workoutStreak);

        double currentWeight = user.getBodyWeight() != null ? user.getBodyWeight().doubleValue() : 0.0;

        return new UserStatsDTO(workoutStreak, weeklyVolume, recoveryScore, currentWeight);
    }

    private int calculateStreak(List<LocalDate> dates) {
        if (dates == null || dates.isEmpty()) return 0;

        int streak = 0;
        LocalDate currentDate = LocalDate.now();

        if (!dates.get(0).equals(currentDate) && !dates.get(0).equals(currentDate.minusDays(1))) {
            return 0;
        }

        LocalDate expectedDate = dates.get(0);
        for (LocalDate date : dates) {
            if (date.equals(expectedDate)) {
                streak++;
                expectedDate = expectedDate.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }

    private int calculateRecovery(int streak) {
        int score = 100 - (streak * 15);
        return Math.max(score, 10);
    }
}