package com.example.demo.dto;

public class ExerciseDistributionDTO {
    private String exerciseName;
    private Integer totalSets;

    public ExerciseDistributionDTO(String exerciseName, Integer totalSets) {
        this.exerciseName = exerciseName;
        this.totalSets = totalSets;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public Integer getTotalSets() {
        return totalSets;
    }

    public void setTotalSets(Integer totalSets) {
        this.totalSets = totalSets;
    }
}
