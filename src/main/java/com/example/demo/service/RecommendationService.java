package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.entity.Workout;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WorkoutRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class RecommendationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.api.key}")
    private String apiKey;

    public Map<String, String> generatePlan(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, String> plan = new HashMap<>();
        plan.put("username", user.getUsername());

        if (user.getGoal() == null) {
            plan.put("error", "No goal set. Update your profile first!");
            return plan;
        }

        String prompt = String.format(
                "You are an expert AI fitness coach. I have a user named %s who weighs %.1f kg. " +
                        "Their primary fitness goal is %s. " +
                        "Provide a short, specific 3-day workout split (e.g., Day 1: Chest/Triceps, Day 2: Back/Biceps, etc.) with 2 key exercises per day. " +
                        "Also provide one specific diet rule. " +
                        "Keep it concise and do not use markdown or asterisks, just plain text.",
                user.getUsername(), user.getBodyWeight(), user.getGoal().name()
        );

        String requestBody = "{ \"contents\": [{ \"parts\": [{ \"text\": \"" + prompt + "\" }] }] }";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        try {
            String fullUrl = apiUrl + apiKey;
            String response = restTemplate.postForObject(fullUrl, request, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            String aiText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();


            Workout workout = new Workout();
            workout.setAiResponse(aiText);
            workout.setUser(user);
            workout.setCreatedAt(LocalDateTime.now());
            workoutRepository.save(workout);


            plan.put("aiRecommendation", aiText);
            plan.put("dataSource", "Google Gemini 1.5 Flash API");

        } catch (Exception e) {
            plan.put("error", "Failed to connect to AI: " + e.getMessage());
        }

        return plan;
    }
}