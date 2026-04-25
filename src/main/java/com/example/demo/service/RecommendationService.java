package com.example.demo.service;

import com.example.demo.entity.Goal;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RecommendationService {

    @Autowired
    private UserRepository userRepository;

    public Map<String, String> generatePlan(Integer userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));


        Map<String, String> plan = new HashMap<>();
        plan.put("username", user.getUsername());


        if (user.getGoal() == null) {
            plan.put("error", "No goal set. Please update your profile with a Goal to receive a customized plan.");
            return plan;
        }


        double weight = user.getBodyWeight().doubleValue();

        if (user.getGoal() == Goal.WEIGHT_LOSS) {
            plan.put("primaryFocus", "Caloric Deficit & High-Intensity Cardio");
            plan.put("dailyCalories", String.format("%.0f kcal", (weight * 24) - 500)); // Standard formula
            plan.put("workoutRecommendation", "4 days of mixed cardio, 2 days of light resistance training.");

        } else if (user.getGoal() == Goal.MUSCLE_GAIN) {
            plan.put("primaryFocus", "Caloric Surplus & Heavy Lifting");
            plan.put("dailyProtein", String.format("%.0f grams", weight * 2.2)); // ~2.2g per kg for muscle gain
            plan.put("workoutRecommendation", "5 days of heavy hypertrophy weightlifting (Push/Pull/Legs).");

        } else if (user.getGoal() == Goal.MAINTENANCE) {
            plan.put("primaryFocus", "Body Recomposition");
            plan.put("dailyCalories", String.format("%.0f kcal", weight * 24));
            plan.put("workoutRecommendation", "3 days of full-body lifting, 2 days of moderate cardio.");
        }

        return plan;
    }
}