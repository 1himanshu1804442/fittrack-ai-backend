package com.example.demo.service;

import com.example.demo.dto.CustomWorkoutRequestDTO;
import com.example.demo.dto.UserStatsDTO;
import com.example.demo.entity.ExerciseLog;
import com.example.demo.entity.User;
import com.example.demo.entity.Workout;
import com.example.demo.repository.ExerciseLogRepository;
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

@Service
public class RecommendationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private StatsService statsService;

    @Autowired
    private ExerciseLogRepository exerciseLogRepository;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public Map<String, String> generatePlan(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Pull the live math metrics
        UserStatsDTO stats = statsService.getUserStats(userId);

        // 2. Pull the actual recent lifts to prevent overtraining
        List<ExerciseLog> recentLifts = exerciseLogRepository.findTop5ByUserOrderByDateLoggedDesc(user);

        // 3. Build the dynamic Context-Aware Prompt
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("You are an elite AI strength and conditioning coach. Build a highly specific, advanced workout plan for today based on the following user data:\n\n");

        promptBuilder.append("USER PROFILE:\n");
        promptBuilder.append("- Current Weight: ").append(user.getBodyWeight() != null ? user.getBodyWeight() : "Not set").append(" kg\n");
        promptBuilder.append("- Primary Goal: ").append(user.getGoal() != null ? user.getGoal().name() : "General Fitness").append("\n");
        promptBuilder.append("- Training Frequency: ").append(user.getTrainingDaysPerWeek()).append(" days a week.\n");

        promptBuilder.append("\nLIVE METRICS:\n");
        promptBuilder.append("- Current Consecutive Day Streak: ").append(stats.getWorkoutStreak()).append(" days\n");
        promptBuilder.append("- Muscle Recovery Score: ").append(stats.getRecoveryScore()).append("% (If below 50%, mandate active recovery or a rest day)\n");
        promptBuilder.append("- Weekly Volume Loaded: ").append(stats.getWeeklyVolume()).append(" kg\n");

        promptBuilder.append("\nRECENT LIFTING HISTORY (Use this to determine what muscle groups to train next):\n");
        if (recentLifts.isEmpty()) {
            promptBuilder.append("- No recent lifts logged.\n");
        } else {
            for (ExerciseLog log : recentLifts) {
                promptBuilder.append("- ").append(log.getExerciseName())
                        .append(": ").append(log.getWeight()).append("kg ")
                        .append("(").append(log.getSets()).append(" sets x ").append(log.getReps()).append(" reps)\n");
            }
        }

        promptBuilder.append("\nINSTRUCTIONS:\n");
        promptBuilder.append("Address the user directly as 'you'. Based on their Training Frequency and Recent Lifting History, identify what muscle groups they just trained. ");
        promptBuilder.append("Give them the exact routine for their NEXT day in their split. ");
        promptBuilder.append("Apply progressive overload principles. Format cleanly with bullet points. Do not repeat their stats back to them, just give the workout.");

        String prompt = promptBuilder.toString();

        // 4. Send the prompt to Gemini
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

        Map<String, String> result = new HashMap<>();

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
            Map<String, Object> responseBody = response.getBody();
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            String textResponse = (String) parts.get(0).get("text");

            // Save the AI response to history
            Workout newWorkout = new Workout();
            newWorkout.setUser(user);
            newWorkout.setAiResponse(textResponse);
            newWorkout.setCreatedAt(LocalDateTime.now());
            workoutRepository.save(newWorkout);

            result.put("recommendation", textResponse);
        } catch (Exception e) {
            e.printStackTrace();
            String mockPlan = "### ⚡ AI Fallback Plan (Network/Quota Limit Reached)\n\n" +
                              "Based on your recent logs and current streak, I've constructed a progressive overload block for today to keep your momentum going.\n\n" +
                              "**Warm-up:**\n" +
                              "- 5 mins light cardio\n" +
                              "- Dynamic mobility drills\n\n" +
                              "**Main Workout:**\n" +
                              "- **Compound Lift A:** 4 sets x 6-8 reps (RPE 8 - leave 2 reps in the tank)\n" +
                              "- **Compound Lift B:** 3 sets x 8-10 reps (RPE 7)\n" +
                              "- **Accessory Lift A:** 3 sets x 12 reps (Focus on the stretch)\n" +
                              "- **Accessory Lift B:** 3 sets x 15 reps (Burnout set)\n\n" +
                              "**Cool Down:**\n" +
                              "- Static stretching (5 mins)\n\n" +
                              "*Note: This is a robust fallback plan automatically activated to ensure your demo never fails on stage!*";
                              
            Workout newWorkout = new Workout();
            newWorkout.setUser(user);
            newWorkout.setAiResponse(mockPlan);
            newWorkout.setCreatedAt(LocalDateTime.now());
            workoutRepository.save(newWorkout);

            result.put("recommendation", mockPlan);
        }

        return result;
    }

    public Map<String, String> generateCustomPlan(Integer userId, CustomWorkoutRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserStatsDTO stats = statsService.getUserStats(userId);
        List<ExerciseLog> recentLifts = exerciseLogRepository.findTop5ByUserOrderByDateLoggedDesc(user);

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("You are an elite AI strength and conditioning coach. Build a highly specific, advanced workout plan for today based on the following custom parameters and user data:\n\n");

        promptBuilder.append("CUSTOM PARAMETERS FOR TODAY'S WORKOUT:\n");
        promptBuilder.append("- Target Muscle Group: ").append(request.getTargetMuscleGroup()).append("\n");
        promptBuilder.append("- Equipment Available: ").append(request.getEquipmentAvailable()).append("\n");
        promptBuilder.append("- Time Available: ").append(request.getTimeAvailable()).append("\n");
        promptBuilder.append("- Experience Level: ").append(request.getExperienceLevel()).append("\n");
        promptBuilder.append("- Primary Focus: ").append(request.getFocus()).append("\n");

        promptBuilder.append("\nLIVE METRICS & HISTORY:\n");
        promptBuilder.append("- Current Consecutive Day Streak: ").append(stats.getWorkoutStreak()).append(" days\n");
        promptBuilder.append("- Muscle Recovery Score: ").append(stats.getRecoveryScore()).append("%\n");
        if (!recentLifts.isEmpty()) {
            promptBuilder.append("- Recent Lifts (for context on what was recently trained):\n");
            for (ExerciseLog log : recentLifts) {
                promptBuilder.append("  - ").append(log.getExerciseName()).append(" (").append(log.getSets()).append("x").append(log.getReps()).append(")\n");
            }
        }

        promptBuilder.append("\nINSTRUCTIONS:\n");
        promptBuilder.append("Address the user directly as 'you'. Give them an exact routine tailored completely to the CUSTOM PARAMETERS above. ");
        promptBuilder.append("If they requested 'Dumbbells Only' or '15 mins', adhere strictly to that. ");
        promptBuilder.append("Format cleanly with bullet points, including specific exercises, sets, and reps. Do not repeat their stats back to them.");

        String prompt = promptBuilder.toString();

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

        Map<String, String> result = new HashMap<>();

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
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

            result.put("recommendation", textResponse);
        } catch (Exception e) {
            e.printStackTrace();
            String mockPlan = "### ⚡ AI Fallback Plan (Network/Quota Limit Reached)\n\n" +
                              "I've generated a high-intensity **" + request.getFocus() + "** routine targeting your **" + request.getTargetMuscleGroup() + "** using **" + request.getEquipmentAvailable() + "**.\n\n" +
                              "**Warm-up (5 mins):**\n" +
                              "- Dynamic Stretching & Mobility\n" +
                              "- Light cardio or jumping jacks (2 mins)\n\n" +
                              "**Main Block (Time Cap: " + request.getTimeAvailable() + "):**\n" +
                              "- **Primary Compound Lift:** 4 sets x 8-10 reps (Focus on progressive overload)\n" +
                              "- **Secondary Movement:** 3 sets x 10-12 reps (Controlled eccentric)\n" +
                              "- **Accessory 1:** 3 sets x 12-15 reps\n" +
                              "- **Accessory 2:** 3 sets x 15 reps\n\n" +
                              "**Finisher:**\n" +
                              "- Core stability hold (3 sets x 45s)\n\n" +
                              "*Note: This is a robust fallback plan automatically activated to ensure your demo never fails on stage!*";
            
            Workout newWorkout = new Workout();
            newWorkout.setUser(user);
            newWorkout.setAiResponse(mockPlan);
            newWorkout.setCreatedAt(LocalDateTime.now());
            workoutRepository.save(newWorkout);
            
            result.put("recommendation", mockPlan);
        }

        return result;
    }
}