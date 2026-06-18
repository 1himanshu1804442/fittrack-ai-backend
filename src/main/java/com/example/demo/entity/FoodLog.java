package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "food_logs")
public class FoodLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String foodName;

    @Column(nullable = false)
    private Integer calories;

    @Column(nullable = false)
    private Double protein;

    @Column(nullable = false)
    private Double carbs;

    @Column(nullable = false)
    private Double fat;

    @Column(nullable = false)
    private LocalDateTime dateLogged;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    public FoodLog() {
        this.dateLogged = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public Integer getCalories() { return calories; }
    public void setCalories(Integer calories) { this.calories = calories; }

    public Double getProtein() { return protein; }
    public void setProtein(Double protein) { this.protein = protein; }

    public Double getCarbs() { return carbs; }
    public void setCarbs(Double carbs) { this.carbs = carbs; }

    public Double getFat() { return fat; }
    public void setFat(Double fat) { this.fat = fat; }

    public LocalDateTime getDateLogged() { return dateLogged; }
    public void setDateLogged(LocalDateTime dateLogged) { this.dateLogged = dateLogged; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
