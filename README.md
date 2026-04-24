# 🏋️‍♂️ FitTrack AI Backend

A scalable, RESTful backend API designed to power a smart fitness tracking application. It provides user management and will feature AI-driven workout recommendations based on specific body metrics and fitness goals.

## 🚀 Tech Stack
* **Language:** Java 17
* **Framework:** Spring Boot 3
* **Database:** PostgreSQL
* **ORM:** Spring Data JPA / Hibernate

## ✨ Current Features (Phase 1)
* **Three-Tier Architecture:** Clean separation of Controller, Service, and Repository layers.
* **User Management:** REST API endpoints for creating and retrieving users via JSON payload.
* **Goal Tracking:** Integration of programmatic `Enum` goals (WEIGHT_LOSS, MUSCLE_GAIN, MAINTENANCE) to drive future AI logic.
* **Secure Configurations:** Environment variable implementation for database credentials following the Twelve-Factor App methodology.

## 🛠️ Local Setup
1. Clone the repository.
2. Create a local PostgreSQL database.
3. Set your environment variable `DB_PASSWORD` in your IDE run configurations.
4. Run the Spring Boot application on port `8080`.