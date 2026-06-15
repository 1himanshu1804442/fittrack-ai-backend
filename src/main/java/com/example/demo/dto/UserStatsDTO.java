package com.example.demo.dto;

public class UserStatsDTO {
    private int workoutStreak;
    private int weeklyVolume;
    private int recoveryScore;
    private double currentWeight;

    public UserStatsDTO(int workoutStreak, int weeklyVolume, int recoveryScore, double currentWeight) {
        this.workoutStreak = workoutStreak;
        this.weeklyVolume = weeklyVolume;
        this.recoveryScore = recoveryScore;
        this.currentWeight = currentWeight;
    }

    public int getWorkoutStreak() { return workoutStreak; }
    public void setWorkoutStreak(int workoutStreak) { this.workoutStreak = workoutStreak; }
    public int getWeeklyVolume() { return weeklyVolume; }
    public void setWeeklyVolume(int weeklyVolume) { this.weeklyVolume = weeklyVolume; }
    public int getRecoveryScore() { return recoveryScore; }
    public void setRecoveryScore(int recoveryScore) { this.recoveryScore = recoveryScore; }
    public double getCurrentWeight() { return currentWeight; }
    public void setCurrentWeight(double currentWeight) { this.currentWeight = currentWeight; }
}