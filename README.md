# FitTrack AI — Backend 🏋️‍♂️🧠

> **An intelligent full-stack fitness platform powered by Spring Boot, PostgreSQL, and Google Gemini AI that generates personalized, context-aware workout plans based on real user training data.**

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Gemini AI](https://img.shields.io/badge/Google_Gemini-AI-4285F4?style=for-the-badge&logo=google&logoColor=white)](https://ai.google.dev/)

---

## 🎯 What Makes This Different

This is **not** a basic CRUD workout tracker. FitTrack AI combines real training analytics with generative AI to create a genuinely intelligent fitness platform:

| Feature | Basic Fitness Apps | FitTrack AI |
|---|---|---|
| Workout Plans | Hardcoded templates | AI-generated, personalized to YOUR data |
| Context Awareness | None | Feeds recovery score, streak & lift history into LLM |
| Failure Handling | App crashes on API error | Intelligent fallback mock system for 429 errors |
| Nutrition Tracking | Manual entry only | USDA FoodData Central API integration |
| Security | Session cookies | Stateless JWT authentication |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    React Frontend                        │
│         (Vite + TailwindCSS + Recharts)                 │
└────────────────────────┬────────────────────────────────┘
                         │ REST API (JWT Bearer Token)
                         ▼
┌─────────────────────────────────────────────────────────┐
│                Spring Boot Backend                       │
│                                                          │
│  ┌──────────┐  ┌──────────────┐  ┌───────────────────┐  │
│  │Controller│──│  Service      │──│   Repository      │  │
│  │  Layer   │  │  Layer        │  │   Layer (JPA)     │  │
│  └──────────┘  └──────┬───────┘  └─────────┬─────────┘  │
│                       │                     │            │
│              ┌────────▼────────┐   ┌───────▼────────┐   │
│              │ Gemini AI API   │   │  PostgreSQL DB  │   │
│              │ (with Fallback) │   │                 │   │
│              └─────────────────┘   └────────────────┘   │
│                                                          │
│  ┌─────────────────┐  ┌──────────────────────────────┐  │
│  │  JWT Security    │  │  USDA FoodData Central API   │  │
│  │  (Stateless)     │  │  (External Nutrition Data)   │  │
│  └─────────────────┘  └──────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

---

## 🚀 Key Features

### 1. Context-Aware AI Workout Generation
The AI engine doesn't just generate random plans. It pulls the user's **real data** from the database and constructs a rich prompt:
- Current body weight & fitness goal
- Training days per week
- Workout streak & recovery score (calculated from exercise frequency)
- Last 10 exercise logs with actual weights, sets, and reps

This data is injected into the Gemini prompt so the AI can recommend progressive overload, deload weeks, or volume adjustments based on real training patterns.

### 2. Defensive Fallback System
External APIs fail. Gemini has rate limits (429 errors). Instead of crashing, the backend:
- Catches `429 Too Many Requests` responses
- Dynamically constructs a realistic mock workout plan
- Saves it to the database as a normal workout
- Returns it seamlessly — the user never knows

### 3. USDA Nutrition API Integration
Food logging uses the **USDA FoodData Central API** (free, no key required):
- Search 300,000+ real foods by name
- Auto-fills calories, protein, carbs, and fat per 100g
- Logs are stored in PostgreSQL with user association

### 4. Stateless JWT Authentication
- `JwtUtil.java` — Token generation and validation using HMAC-SHA256
- `JwtRequestFilter.java` — OncePerRequestFilter that extracts and validates JWT from Authorization header
- `SecurityConfig.java` — Stateless session policy, CORS configuration, endpoint protection

### 5. Smart Exercise Logging
- **Quick Log**: Backend endpoint returns the user's most recent exercises with last-used weight, enabling one-tap logging
- **Full CRUD**: Create, Read (paginated), Update, Delete exercise logs
- **Auto-timestamping**: Each log automatically records the date

---

## 📁 Project Structure

```
src/main/java/com/example/demo/
├── controller/
│   ├── UserController.java          # Auth, profile, AI workout endpoints
│   ├── ExerciseController.java      # CRUD + Quick Log + Stats + Analytics
│   ├── FoodLogController.java       # Nutrition logging + USDA API proxy
│   └── StatsController.java         # Dashboard metrics endpoint
├── dto/
│   ├── UserUpdateDTO.java           # Profile update payload
│   ├── CustomWorkoutRequestDTO.java # AI workout customization params
│   ├── QuickLogDataDTO.java         # Quick-log response shape
│   └── ExerciseDistributionDTO.java # Analytics chart data
├── entity/
│   ├── User.java                    # User entity with macro goals
│   ├── Workout.java                 # AI-generated workout plans
│   ├── ExerciseLog.java             # Individual lift records
│   ├── FoodLog.java                 # Nutrition log entries
│   └── Goal.java                    # Enum: MUSCLE_GAIN, WEIGHT_LOSS, MAINTENANCE
├── repository/
│   ├── UserRepository.java
│   ├── WorkoutRepository.java
│   ├── ExerciseLogRepository.java
│   └── FoodLogRepository.java
├── security/
│   ├── SecurityConfig.java          # Spring Security + CORS + JWT filter chain
│   ├── JwtUtil.java                 # Token generation & validation
│   └── JwtRequestFilter.java       # Per-request JWT extraction filter
└── service/
    ├── UserService.java             # User CRUD + UserDetailsService
    ├── RecommendationService.java   # Gemini AI integration + fallback logic
    └── StatsService.java            # Streak, volume, recovery calculations
```

---

## 🔌 API Endpoints

### Authentication
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/users/register` | Register new user |
| `POST` | `/api/users/login` | Authenticate & receive JWT + userId |

### User Profile
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/users/{id}` | Get user profile (including macro goals) |
| `PUT` | `/api/users/{id}` | Update weight, goal, training days, macro targets |

### Exercise Logging
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/users/{id}/exercises` | Log a new lift |
| `GET` | `/api/users/{id}/exercises?page=0&size=10` | Paginated exercise history |
| `PUT` | `/api/users/{id}/exercises/{logId}` | Edit a logged lift |
| `DELETE` | `/api/users/{id}/exercises/{logId}` | Delete a lift |
| `GET` | `/api/users/{id}/exercises/quick-log-data` | Recent exercises with last weight |

### AI Workouts
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/users/{id}/recommendation` | Generate context-aware AI plan |
| `POST` | `/api/users/{id}/custom-workout` | Generate plan with custom parameters |
| `GET` | `/api/users/{id}/history` | Retrieve all past AI-generated plans |

### Analytics
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/users/{id}/stats` | Streak, weekly volume, recovery score |
| `GET` | `/api/users/{id}/exercises/volume-data` | Volume over time (chart data) |
| `GET` | `/api/users/{id}/exercises/distribution` | Exercise distribution by muscle group |

### Nutrition
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/users/{id}/food` | Log a food entry |
| `GET` | `/api/users/{id}/food?period=daily` | Get food logs (daily/weekly/monthly) |
| `DELETE` | `/api/users/{id}/food/{logId}` | Delete a food entry |
| `GET` | `/api/food/search?query=chicken` | Proxy search to USDA API |

---

## ⚙️ Setup & Installation

### Prerequisites
- Java 17+
- PostgreSQL 14+
- Google Gemini API Key ([Get one free](https://aistudio.google.com/apikey))

### 1. Clone the repository
```bash
git clone https://github.com/1himanshu1804442/fittrack-ai-backend.git
cd fittrack-ai-backend
```

### 2. Configure the database
Create a PostgreSQL database:
```sql
CREATE DATABASE fittrack_db;
```

### 3. Set environment variables
Update `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/fittrack_db
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD
spring.jpa.hibernate.ddl-auto=update

gemini.api.key=YOUR_GEMINI_API_KEY
jwt.secret=YOUR_JWT_SECRET_KEY
```

### 4. Run the application
```bash
./mvnw spring-boot:run
```
The server will start at `http://localhost:8080`

---

## 🧪 Tech Stack

| Layer | Technology |
|---|---|
| **Runtime** | Java 17 |
| **Framework** | Spring Boot 3.x |
| **Security** | Spring Security + JWT (HMAC-SHA256) |
| **Database** | PostgreSQL with Spring Data JPA / Hibernate |
| **AI Engine** | Google Gemini 2.0 Flash (REST API) |
| **Nutrition API** | USDA FoodData Central |
| **Build Tool** | Maven |

---

## 🔗 Related

- **Frontend Repository**: [fittrack-ai-frontend](https://github.com/1himanshu1804442/fittrack-ai-frontend)

---

## 👤 Author

**Himanshu Yadav**  
- GitHub: [@1himanshu1804442](https://github.com/1himanshu1804442)

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).