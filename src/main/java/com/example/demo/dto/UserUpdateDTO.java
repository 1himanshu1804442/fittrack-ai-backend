package com.example.demo.dto;

import com.example.demo.entity.Goal;
import java.math.BigDecimal;

public class UserUpdateDTO {
    private BigDecimal bodyWeight;
    private Goal goal;
    private Integer trainingDaysPerWeek;

    public BigDecimal getBodyWeight() { return bodyWeight; }
    public void setBodyWeight(BigDecimal bodyWeight) { this.bodyWeight = bodyWeight; }

    public Goal getGoal() { return goal; }
    public void setGoal(Goal goal) { this.goal = goal; }

    public Integer getTrainingDaysPerWeek() { return trainingDaysPerWeek; }
    public void setTrainingDaysPerWeek(Integer trainingDaysPerWeek) { this.trainingDaysPerWeek = trainingDaysPerWeek; }
}