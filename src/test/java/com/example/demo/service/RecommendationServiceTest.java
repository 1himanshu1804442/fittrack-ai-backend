package com.example.demo.service;

import com.example.demo.dto.UserStatsDTO;
import com.example.demo.entity.User;
import com.example.demo.entity.Workout;
import com.example.demo.repository.ExerciseLogRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WorkoutRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecommendationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private ExerciseLogRepository exerciseLogRepository;

    @Mock
    private StatsService statsService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private RecommendationService recommendationService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setUserId(1);
        mockUser.setUsername("testuser");
        
        ReflectionTestUtils.setField(recommendationService, "geminiApiKey", "fake-api-key");
    }

    @Test
    void generatePlan_ThrowsException_TriggersFallback() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(statsService.getUserStats(1)).thenReturn(new UserStatsDTO(0, 0, 0, 0.0));
        when(exerciseLogRepository.findTop5ByUserOrderByDateLoggedDesc(mockUser)).thenReturn(Collections.emptyList());
        
        // Act
        Map<String, String> result = recommendationService.generatePlan(1);

        // Assert
        assertTrue(result.containsKey("recommendation"));
        String recommendation = result.get("recommendation");
        
        // Verify it triggered our defensive fallback string
        assertTrue(recommendation.contains("⚡ AI Fallback Plan"));
        
        // Verify the fallback plan was saved to the DB
        verify(workoutRepository, times(1)).save(any(Workout.class));
    }
}
