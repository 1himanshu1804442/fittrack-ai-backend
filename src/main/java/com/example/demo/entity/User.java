package com.example.demo.entity; // Update this line if your package name is different

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "users") // Maps explicitly to your PostgreSQL "users" table
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "username", length = 50, nullable = false)
    private String username;

    @Column(name = "body_weight", precision = 5, scale = 2)
    private BigDecimal bodyWeight;
}