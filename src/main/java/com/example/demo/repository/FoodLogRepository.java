package com.example.demo.repository;

import com.example.demo.entity.FoodLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FoodLogRepository extends JpaRepository<FoodLog, Long> {
    
    @Query("SELECT f FROM FoodLog f WHERE f.user.userId = :userId AND f.dateLogged >= :startOfDay AND f.dateLogged <= :endOfDay ORDER BY f.dateLogged DESC")
    List<FoodLog> findByUserIdAndDateLoggedBetween(
            @Param("userId") Integer userId, 
            @Param("startOfDay") LocalDateTime startOfDay, 
            @Param("endOfDay") LocalDateTime endOfDay
    );
}
