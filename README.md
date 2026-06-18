# 🏋️‍♂️ FitTrack AI Backend

A scalable, RESTful backend API designed to power a smart fitness tracking application. It provides user management, exercise logging, and features AI-driven workout recommendations based on specific body metrics and fitness goals.

## 🚀 Tech Stack
* **Language:** Java 17
* **Framework:** Spring Boot 3
* **Database:** PostgreSQL
* **ORM:** Spring Data JPA / Hibernate

## ✨ Current Features
* **Three-Tier Architecture:** Clean separation of Controller, Service, and Repository layers.
* **Authentication:** Secure JWT-based authentication for multiple users.
* **Workout Logging:** Full CRUD capabilities for exercise logs with paginated endpoints.
* **Analytics:** Generates Volume Progression and 1-Rep Max (1RM) progression data utilizing the Brzycki formula.
* **AI Coach:** Integration with Google Gemini AI to generate customized workout plans.

## 🛠️ Local Setup
1. Clone the repository.
2. Create a local PostgreSQL database.
3. Set your environment variables `DB_PASSWORD` and `GEMINI_API_KEY` in your IDE run configurations.
4. Run the Spring Boot application on port `8080`.