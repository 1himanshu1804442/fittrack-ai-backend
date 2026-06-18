package com.example.demo.service;

import com.example.demo.dto.UserUpdateDTO;
import com.example.demo.entity.User;
import com.example.demo.entity.Workout;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WorkoutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                new ArrayList<>()
        );
    }

    public User createNewUser(User user) {
        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Workout addWorkoutToUser(Integer userId, Workout workout) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        workout.setUser(user);
        return workoutRepository.save(workout);
    }

    public User updateUser(Integer id, UserUpdateDTO updateData) {
        return userRepository.findById(id)
                .map(user -> {
                    // Update existing fields
                    user.setBodyWeight(updateData.getBodyWeight());
                    user.setGoal(updateData.getGoal());

                    // Safely update the new training frequency field
                    if (updateData.getTrainingDaysPerWeek() != null) {
                        user.setTrainingDaysPerWeek(updateData.getTrainingDaysPerWeek());
                    }

                    if (updateData.getCalorieGoal() != null) user.setCalorieGoal(updateData.getCalorieGoal());
                    if (updateData.getProteinGoal() != null) user.setProteinGoal(updateData.getProteinGoal());
                    if (updateData.getCarbsGoal() != null) user.setCarbsGoal(updateData.getCarbsGoal());
                    if (updateData.getFatGoal() != null) user.setFatGoal(updateData.getFatGoal());

                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id)); // Adding a throw here is safer than returning null!
    }
}