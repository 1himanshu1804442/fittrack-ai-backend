package com.example.demo.repository;

import com.example.demo.entity.ExerciseLog;
import com.example.demo.entity.User; // <-- This import is required!
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExerciseLogRepository extends JpaRepository<ExerciseLog, Long> {

    List<ExerciseLog> findTop5ByUserOrderByDateLoggedDesc(User user);

    List<ExerciseLog> findAllByUserUserIdOrderByDateLoggedDesc(Integer userId);

    @Query("SELECT DISTINCT e.exerciseName FROM ExerciseLog e WHERE e.user.userId = :userId ORDER BY e.exerciseName ASC")
    List<String> findDistinctExerciseNamesByUserId(@Param("userId") Integer userId);

    List<ExerciseLog> findByUserUserIdAndExerciseNameOrderByDateLoggedAsc(Integer userId, String exerciseName);

    Page<ExerciseLog> findByUserUserIdOrderByDateLoggedDesc(Integer userId, Pageable pageable);

    @Query("SELECT e.dateLogged FROM ExerciseLog e WHERE e.user.userId = :userId ORDER BY e.dateLogged DESC")
    List<LocalDateTime> findAllLogDatesByUserId(@Param("userId") Integer userId);

    @Query("SELECT COALESCE(SUM(e.weight * e.sets * e.reps), 0) FROM ExerciseLog e WHERE e.user.userId = :userId AND e.dateLogged >= :since")
    Double calculateTotalVolumeSince(@Param("userId") Integer userId, @Param("since") LocalDateTime since);

    @Query("SELECT COALESCE(SUM(e.weight * e.sets * e.reps), 0) FROM ExerciseLog e WHERE e.user.userId = :userId AND e.dateLogged >= :startDate AND e.dateLogged < :endDate")
    Double calculateTotalVolumeBetween(@Param("userId") Integer userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}