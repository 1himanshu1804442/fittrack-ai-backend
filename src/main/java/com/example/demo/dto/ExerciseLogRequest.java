package com.example.demo.dto;

import lombok.Data;

@Data
public class ExerciseLogRequest {

    private String exerciseName;
    private Double weight;
    private Integer sets;
    private Integer reps;

}