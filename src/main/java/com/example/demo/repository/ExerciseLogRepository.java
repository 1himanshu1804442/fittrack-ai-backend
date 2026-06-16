package com.example.demo.repository;

import com.example.demo.entity.ExerciseLog;
import com.example.demo.entity.User; // <-- This import is required!
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExerciseLogRepository extends JpaRepository<ExerciseLog, Long> {

    List<ExerciseLog> findTop5ByUserOrderByDateLoggedDesc(User user);

    List<ExerciseLog> findByUserUserIdOrderByDateLoggedDesc(Integer userId);

    @Query("SELECT e.dateLogged FROM ExerciseLog e WHERE e.user.userId = :userId ORDER BY e.dateLogged DESC")
    List<LocalDateTime> findAllLogDatesByUserId(@Param("userId") Integer userId);

    @Query("SELECT COALESCE(SUM(e.weight * e.sets * e.reps), 0) FROM ExerciseLog e WHERE e.user.userId = :userId AND e.dateLogged >= :since")
    Double calculateTotalVolumeSince(@Param("userId") Integer userId, @Param("since") LocalDateTime since);
}