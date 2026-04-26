package com.example.demo.dto;

import com.example.demo.entity.Goal;
import java.math.BigDecimal; // Add this import

public class UserUpdateDTO {
    private BigDecimal bodyWeight; // Change Double to BigDecimal
    private Goal goal;

    // Update Getters and Setters to use BigDecimal
    public BigDecimal getBodyWeight() { return bodyWeight; }
    public void setBodyWeight(BigDecimal bodyWeight) { this.bodyWeight = bodyWeight; }

    public Goal getGoal() { return goal; }
    public void setGoal(Goal goal) { this.goal = goal; }
}