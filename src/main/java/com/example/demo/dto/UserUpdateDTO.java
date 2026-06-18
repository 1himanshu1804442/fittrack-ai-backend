package com.example.demo.dto;

import com.example.demo.entity.Goal;
import java.math.BigDecimal;

public class UserUpdateDTO {
    private BigDecimal bodyWeight;
    private Goal goal;
    private Integer trainingDaysPerWeek;
    private Integer calorieGoal;
    private Integer proteinGoal;
    private Integer carbsGoal;
    private Integer fatGoal;

    public BigDecimal getBodyWeight() { return bodyWeight; }
    public void setBodyWeight(BigDecimal bodyWeight) { this.bodyWeight = bodyWeight; }

    public Goal getGoal() { return goal; }
    public void setGoal(Goal goal) { this.goal = goal; }

    public Integer getTrainingDaysPerWeek() { return trainingDaysPerWeek; }
    public void setTrainingDaysPerWeek(Integer trainingDaysPerWeek) { this.trainingDaysPerWeek = trainingDaysPerWeek; }

    public Integer getCalorieGoal() { return calorieGoal; }
    public void setCalorieGoal(Integer calorieGoal) { this.calorieGoal = calorieGoal; }

    public Integer getProteinGoal() { return proteinGoal; }
    public void setProteinGoal(Integer proteinGoal) { this.proteinGoal = proteinGoal; }

    public Integer getCarbsGoal() { return carbsGoal; }
    public void setCarbsGoal(Integer carbsGoal) { this.carbsGoal = carbsGoal; }

    public Integer getFatGoal() { return fatGoal; }
    public void setFatGoal(Integer fatGoal) { this.fatGoal = fatGoal; }
}