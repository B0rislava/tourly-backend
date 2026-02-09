<div align="center">

# Tourly Backend

### *Tour Management Platform*

[![Spring Boot](https://img.shields.io/badge/Spring--Boot-4.0.0-brightgreen.svg?style=for-the-badge&logo=spring)](https://spring.io/projects/spring-boot)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-purple.svg?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg?style=for-the-badge&logo=openjdk)](https://adoptium.net/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-latest-blue.svg?style=for-the-badge&logo=postgresql)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-enabled-2496ED.svg?style=for-the-badge&logo=docker)](https://www.docker.com/)

*Powering the future of travel experiences with robust APIs and real-time capabilities*

[Features](#key-features) • [Tech Stack](#tech-stack) • [Setup](#setup--installation) • [API Docs](#api-documentation)

</div>

---

## Related Projects

<table>
<tr>
<td width="50%" align="center">
<img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android"/>
<h3>Tourly Android</h3>
<p>Android client</p>
<a href="https://github.com/B0rislava/tourly-android">
<img src="https://img.shields.io/badge/View_Repository-181717?style=for-the-badge&logo=github" alt="Repo"/>
</a>
</td>
<td width="50%" align="center">
<img src="https://img.shields.io/badge/API-FF6C37?style=for-the-badge&logo=postman&logoColor=white" alt="API"/>
<h3>Tourly Backend</h3>
<p>RESTful API</p>
<a href="https://github.com/B0rislava/tourly-backend">
<img src="https://img.shields.io/badge/Current_Repository-6366f1?style=for-the-badge" alt="Current"/>
</a>
</td>
</tr>
</table>

---

## Key Features

<table>
<tr>
<td width="50%">

### **Security & Auth**
- JWT-based authentication
- OAuth2 Google integration
- Role-based access control

### **Tour Management**
- Create & update tours
- Category & tag support
- Advanced discovery features

### **Real-time Communication**
- WebSocket-powered chat
- Guide-traveler messaging
- Instant notifications

### **Engagement**
- Push notification system
- Email automation
- Activity tracking

</td>
<td width="50%">

### **Booking System**
- Full lifecycle management
- Reservation handling
- Payment integration ready

### **Reviews & Ratings**
- Community feedback
- Rating aggregation
- Review moderation

### **Media Handling**
- Cloudinary integration
- Image optimization
- Multi-format support

### **Email Services**
- Verification emails
- Transactional messages
- Template management

</td>
</tr>
</table>

---

## Tech Stack

<div align="center">

| Category | Technology | Version |
|:--------:|:-----------|:-------:|
| **Framework** | Spring Boot | 4.0.0 |
| **Language** | Kotlin | 2.2.21 |
| **Runtime** | Java (Eclipse Temurin) | 21 |
| **Build Tool** | Gradle | 9.2.1 |
| **Database** | PostgreSQL | latest |
| **Security** | Spring Security + JJWT | 0.11.5 |
| **API Docs** | Springdoc OpenAPI | 2.8.4 |
| **Storage** | Cloudinary | 2.0.0 |
| **Real-time** | Spring WebSockets | - |
| **Container** | Docker | - |

</div>

---

## Project Structure

```
api/
├── src/main/kotlin/com/tourly/core/
│   ├── api/             # REST Controllers & DTOs
│   ├── config/          # Configuration (CORS, Security, etc.)
│   ├── data/            # Entities, Repositories & Mappers
│   ├── exception/       # Global Error Handling
│   ├── scheduler/       # Background Jobs & Tasks
│   ├── security/        # JWT & Authentication
│   └── service/         # Business Logic Layer
└── src/main/resources/
    ├── templates/       # Email & HTML Templates
    └── application.yaml # Main Configuration
```

---

## Setup & Installation

### Prerequisites

Ensure you have the following installed:

```bash
✓ JDK 21 (Eclipse Temurin recommended)
✓ PostgreSQL (Default port: 5434)
✓ Gradle 9.2.1 (Wrapper included)
```

### Environment Configuration

Create a `.env` file in the `api/` directory:

```env
# Service Configuration
SERVICE_NAME=tourly-api
APP_PORT=8080

# Database
DB_PORT=5434
DB_NAME=your_db_name
DB_USERNAME=your_username
DB_PASSWORD=your_password
DB_POOL_SIZE=10

# JWT Configuration
JWT_SECRET=your_secret_key
JWT_ACCESS_TOKEN_EXPIRATION=900000      # 15 minutes
JWT_REFRESH_TOKEN_EXPIRATION=604800000   # 7 days

# Cloudinary
CLOUDINARY_NAME=your_cloud_name
CLOUDINARY_KEY=your_api_key
CLOUDINARY_SECRET=your_api_secret

# Mail Service
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email
MAIL_PASSWORD=your_app_password

# Google OAuth
GOOGLE_CLIENT_ID=your_client_id
GOOGLE_CLIENT_SECRET=your_client_secret

# CORS
ALLOWED_ORIGINS=http://localhost:3000,http://your-app-url.com
```

### Running Locally

```bash
# Navigate to the API directory
cd api

# Run the application
./gradlew bootRun
```

The server will start at `http://localhost:8080`

### Running with Docker

```bash
# Build the Docker image
docker build -t tourly-api .

# Run the container
docker run -p 8080:8080 --env-file .env tourly-api
```

---

## API Documentation

<div align="center">

### Interactive Swagger UI

Once the application is running, explore the API at:

**🔗 [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**
