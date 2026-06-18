package com.example.demo.dto;

public class QuickLogDataDTO {
    private String exerciseName;
    private Double lastWeight;

    public QuickLogDataDTO(String exerciseName, Double lastWeight) {
        this.exerciseName = exerciseName;
        this.lastWeight = lastWeight;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public Double getLastWeight() {
        return lastWeight;
    }

    public void setLastWeight(Double lastWeight) {
        this.lastWeight = lastWeight;
    }
}
