package com.example.demo.service;

import com.example.demo.dto.UserStatsDTO;
import com.example.demo.entity.ExerciseLog;
import com.example.demo.entity.User;
import com.example.demo.repository.ExerciseLogRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.dto.VolumeChartDataDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import com.example.demo.dto.OneRepMaxDataDTO;
import com.example.demo.dto.ExerciseDistributionDTO;
import com.example.demo.dto.QuickLogDataDTO;
import java.util.Map;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.ArrayList;

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

    public int getPreviousWeekVolume(Integer userId) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        LocalDateTime twoWeeksAgo = LocalDateTime.now().minusDays(14);
        Double volume = exerciseLogRepository.calculateTotalVolumeBetween(userId, twoWeeksAgo, oneWeekAgo);
        return volume != null ? volume.intValue() : 0;
    }

    public List<VolumeChartDataDTO> getVolumeChartData(Integer userId) {

        List<ExerciseLog> logs = exerciseLogRepository.findAllByUserUserIdOrderByDateLoggedDesc(userId);


        Map<LocalDate, Double> dailyVolume = logs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.getDateLogged().toLocalDate(),
                        Collectors.summingDouble(log -> log.getWeight() * log.getSets() * log.getReps())
                ));


        return dailyVolume.entrySet().stream()
                .map(entry -> new VolumeChartDataDTO(entry.getKey().toString(), entry.getValue()))
                .sorted(Comparator.comparing(VolumeChartDataDTO::getDate))
                .collect(Collectors.toList());
    }

    public List<OneRepMaxDataDTO> getOneRepMaxAnalytics(Integer userId, String exerciseName) {
        List<ExerciseLog> logs = exerciseLogRepository.findByUserUserIdAndExerciseNameOrderByDateLoggedAsc(userId, exerciseName);

        Map<LocalDate, Double> max1RMPerDay = new HashMap<>();

        for (ExerciseLog log : logs) {
            LocalDate date = log.getDateLogged().toLocalDate();
            // Brzycki formula: Weight * (36 / (37 - Reps))
            double reps = log.getReps() != null ? log.getReps() : 0;
            double weight = log.getWeight() != null ? log.getWeight() : 0;
            
            double oneRepMax;
            if (reps >= 37 || reps == 0) {
                 oneRepMax = weight; 
            } else {
                 oneRepMax = weight * (36.0 / (37.0 - reps));
            }

            max1RMPerDay.put(date, Math.max(max1RMPerDay.getOrDefault(date, 0.0), oneRepMax));
        }

        return max1RMPerDay.entrySet().stream()
                .map(entry -> new OneRepMaxDataDTO(entry.getKey().toString(), Math.round(entry.getValue() * 100.0) / 100.0))
                .sorted(Comparator.comparing(OneRepMaxDataDTO::getDate))
                .collect(Collectors.toList());
    }

    public List<QuickLogDataDTO> getRecentExercisesWithWeights(Integer userId) {
        List<ExerciseLog> logs = exerciseLogRepository.findAllByUserUserIdOrderByDateLoggedDesc(userId);
        
        Map<String, Double> latestWeights = new LinkedHashMap<>();
        for (ExerciseLog log : logs) {
            if (!latestWeights.containsKey(log.getExerciseName()) && log.getWeight() != null) {
                latestWeights.put(log.getExerciseName(), log.getWeight());
            }
        }
        
        return latestWeights.entrySet().stream()
                .limit(5)
                .map(entry -> new QuickLogDataDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public List<ExerciseDistributionDTO> getExerciseDistribution(Integer userId) {
        List<ExerciseLog> logs = exerciseLogRepository.findAllByUserUserIdOrderByDateLoggedDesc(userId);
        
        Map<String, Integer> setsPerExercise = logs.stream()
                .collect(Collectors.groupingBy(
                        ExerciseLog::getExerciseName,
                        Collectors.summingInt(log -> log.getSets() != null ? log.getSets() : 0)
                ));
                
        return setsPerExercise.entrySet().stream()
                .map(entry -> new ExerciseDistributionDTO(entry.getKey(), entry.getValue()))
                .sorted((e1, e2) -> e2.getTotalSets().compareTo(e1.getTotalSets()))
                .limit(10)
                .collect(Collectors.toList());
    }
}