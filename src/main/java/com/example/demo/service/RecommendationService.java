package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class RecommendationService {

    @Autowired
    private UserRepository userRepository;

    // The Outgoing Phone we created earlier!
    @Autowired
    private RestTemplate restTemplate;

    // Pulling your hidden variables from application.properties
    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.api.key}")
    private String apiKey;

    public Map<String, String> generatePlan(Integer userId) {
        // 1. Fetch User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, String> plan = new HashMap<>();
        plan.put("username", user.getUsername());

        if (user.getGoal() == null) {
            plan.put("error", "No goal set. Update your profile first!");
            return plan;
        }

        // 2. PROMPT ENGINEERING: Building the dynamic message
        String prompt = String.format(
                "You are an expert AI fitness coach. I have a user named %s who weighs %.1f kg. " +
                        "Their primary fitness goal is %s. " +
                        "Write a highly motivational 2-sentence workout plan and a 1-sentence daily diet tip for them. " +
                        "Do not use markdown or asterisks, just plain text.",
                user.getUsername(), user.getBodyWeight(), user.getGoal().name()
        );

        // 3. Wrapping the prompt in JSON formatting that Google requires
        String requestBody = "{ \"contents\": [{ \"parts\": [{ \"text\": \"" + prompt + "\" }] }] }";

        // 4. Preparing the HTTP request (telling Google we are sending JSON)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        // 5. Calling the AI and parsing the response!
        try {
            String fullUrl = apiUrl + apiKey;
            // This is where Java actually calls the internet:
            String response = restTemplate.postForObject(fullUrl, request, String.class);

            // Google sends back a massive chunk of data. We just want the text part.
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            String aiText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

            plan.put("aiRecommendation", aiText);
            plan.put("dataSource", "Google Gemini 1.5 Flash API");

        } catch (Exception e) {
            plan.put("error", "Failed to connect to AI: " + e.getMessage());
        }

        return plan;
    }
}