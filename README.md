<div align="center">

# TOURLY Backend

### *Tour Management Platform*

![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![Render](https://img.shields.io/badge/Render-46E3B7?style=for-the-badge&logo=render&logoColor=white)](https://render.com/)

[Features](#key-features) • [Setup](#setup--installation) • [API Docs](#api-documentation)

---

## Related Projects

<div align="center">
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
</div>
</div>


---

## Key Features

<div align="center">
<table>
<tr>
<td align="center" width="25%">

#### 🔐 Security & Auth
- JWT-based authentication
- OAuth2 Google integration
- Role-based access control

</td>
<td align="center" width="25%">

#### 🗺️ Tour Management
- Create & update tours
- Category & tag support
- Advanced discovery features

</td>
<td align="center" width="25%">

#### 💬 Real-time Communication
- WebSocket-powered chat
- Guide-traveler messaging
- Instant notifications

</td>
<td align="center" width="25%">

#### 🔔 Engagement
- Push notification system
- Email automation
- Activity tracking

</td>
</tr>
<tr>
<td align="center" width="25%">

#### 📅 Booking System
- Full lifecycle management
- Reservation handling
- Payment integration ready

</td>
<td align="center" width="25%">

#### ⭐ Reviews & Ratings
- Community feedback
- Rating aggregation
- Review moderation

</td>
<td align="center" width="25%">

#### 🖼️ Media Handling
- Cloudinary integration
- Image optimization
- Multi-format support

</td>
<td align="center" width="25%">

#### 📧 Email Services
- Verification emails
- Transactional messages
- Template management

</td>
</tr>
</table>
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
- JDK 21 (Eclipse Temurin recommended)
- PostgreSQL (Default port: 5434)
- Gradle 9.2.1 (Wrapper included)
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
