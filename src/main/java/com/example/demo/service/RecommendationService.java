package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.entity.Workout;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WorkoutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public Map<String, String> generatePlan(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Workout> recentWorkouts = workoutRepository.findUserHistory(userId)
                .stream()
                .limit(10)
                .collect(Collectors.toList());

        String historyText = recentWorkouts.isEmpty() ? "No previous workouts recorded." :
                recentWorkouts.stream()
                        .map(w -> "Date: " + w.getCreatedAt().toLocalDate() + "\nWorkout: " +
                                w.getAiResponse().substring(0, Math.min(150, w.getAiResponse().length())) + "...")
                        .collect(Collectors.joining("\n\n"));

        String prompt = String.format(
                "You are an expert fitness coach talking directly to your client. Address them as 'you'.\n" +
                        "Their current profile:\n" +
                        "- Weight: %.1f kg\n" +
                        "- Primary Goal: %s\n\n" +
                        "Their recent workout history:\n" +
                        "[%s]\n\n" +
                        "Based on this history, apply progressive overload. " +
                        "Generate a highly structured 3-day split (Push/Pull/Legs). " +
                        "Format cleanly with bullet points. Do not use markdown bolding. Do not repeat their stats back to them, just give the workout.",
                user.getBodyWeight(),
                user.getGoal().name(),
                historyText
        );

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> message = new HashMap<>();
        message.put("text", prompt);

        Map<String, Object> part = new HashMap<>();
        part.put("parts", List.of(message));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(part));

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

        Map<String, String> result = new HashMap<>();

        try {
            Map<String, Object> responseBody = response.getBody();
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            String textResponse = (String) parts.get(0).get("text");


            Workout newWorkout = new Workout();
            newWorkout.setUser(user);
            newWorkout.setAiResponse(textResponse);
            newWorkout.setCreatedAt(LocalDateTime.now());

            workoutRepository.save(newWorkout);
            // -------------------------------------------

            result.put("recommendation", textResponse);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("recommendation", "Failed to generate recommendation.");
        }

        return result;
    }
}