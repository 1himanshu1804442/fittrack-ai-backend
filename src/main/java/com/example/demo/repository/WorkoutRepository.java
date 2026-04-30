package com.example.demo.repository;

import com.example.demo.entity.Workout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {


    @Query(value = "SELECT * FROM workouts WHERE user_id = :userId ORDER BY created_at DESC", nativeQuery = true)
    List<Workout> findUserHistory(@Param("userId") Integer userId);
}