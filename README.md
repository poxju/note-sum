# Note&Sum - AI-Powered Note Management System

A Spring Boot REST API with JWT authentication, role-based access control, and AI-powered text summarization using HuggingFace API.

## 🚀 Features

- **JWT Authentication**: Email/password signup/login with role management
- **Role-Based Access**: ADMIN (see all data) vs AGENT (own data only)
- **AI Text Summarization**: Async background jobs using HuggingFace BART model
- **Status Tracking**: Real-time note processing status (queued → processing → done → failed)
- **PostgreSQL Integration**: Full database with Flyway migrations
- **Docker Deployment**: Containerized application ready for cloud deployment

## 🛠️ Tech Stack

- **Backend**: Spring Boot 3.5.5, Spring Security, Spring Data JPA
- **Database**: PostgreSQL 16 with Flyway migrations
- **AI Integration**: HuggingFace Transformers API (BART-large-CNN)
- **Authentication**: JWT with BCrypt password hashing
- **Containerization**: Docker & Docker Compose
- **Build Tool**: Maven

## 📋 API Endpoints

### Authentication
- `POST /signup` - User registration
- `POST /login` - User login (returns JWT token)
- `POST /logout` - User logout

### Notes Management
- `POST /notes` - Create note (queues AI summarization job)
- `GET /` - List notes (role-based: agents see own, admins see all)
- `GET /actuator/health` - Health check

### Admin Dashboard
- Web interface available at root URL for demo purposes
- Admin users can view all users' notes and system statistics

## 🚀 Quick Start

### Option 1: Docker Deployment (Recommended)

1. **Clone and configure**
   ```bash
   git clone https://github.com/poxju/note-sum.git
   cd note-sum
   ```

2. **Set up environment**
   ```bash
   # Create .env file with your values
   cat > .env << EOF
   DB_PASSWORD=postgres
   JWT_SECRET=your-secure-256-bit-secret-key-here
   JWT_EXPIRATION_MS=your-perfect-time-in-milisecs 
   HUGGINGFACE_API_TOKEN=your_token_here
   SPRING_PROFILES_ACTIVE=production
   EOF
   ```

3. **Deploy with Docker**
   ```bash
   docker-compose up -d
   ```

4. **Access the application**
   - API: http://localhost:8080
   - Health Check: http://localhost:8080/actuator/health

### Option 2: Local Development

```bash
./mvnw spring-boot:run
```

## 🔧 Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `DATABASE_URL` | PostgreSQL connection URL | Yes (auto in Railway) |
| `JWT_SECRET` | JWT signing secret (256+ bits) | Yes |
| `JWT_EXPIRATION_MS` | JWT expiration in milliseconds | No (default: 24h) |
| `HUGGINGFACE_API_TOKEN` | HuggingFace API token | No (fallback available) |
| `SPRING_PROFILES_ACTIVE` | Spring profile | No (default: production) |
| `PORT` | Server port | No (default: 8080) |

## 🗄️ Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'AGENT'))
);
```

### Notes Table
```sql
CREATE TABLE notes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    summary TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'queued',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🤖 AI Summarization Flow

1. **Create Note**: `POST /notes` with title and content
2. **Queue Job**: Note status set to "queued", async processing starts
3. **Processing**: Status changes to "processing", HuggingFace API called
4. **Complete**: Status becomes "done" with generated summary
5. **Error Handling**: Status becomes "failed" if API call fails

## 🔒 Authentication & Authorization

- **First User**: Automatically becomes ADMIN (just for development purposes)
- **JWT Tokens**: Secure authentication with configurable expiration
- **Role-Based Access**: 
  - AGENT: Can only see/manage their own notes
  - ADMIN: Can view all users' notes and access admin dashboard

## 🚀 Cloud Deployment

### Railway Deployment
> **Note**: Railway deployment is currently not working due to configuration issues. Local Docker deployment is fully functional.

1. Connect GitHub repository to Railway
2. Add PostgreSQL service
3. Set environment variables
4. Deploy automatically on git push

### Environment Setup for Production
```bash
# Railway will auto-provide DATABASE_URL
JWT_SECRET=your-256-bit-secret
HUGGINGFACE_API_TOKEN=your-hf-token
SPRING_PROFILES_ACTIVE=production
```

## 🧪 Testing the API

### 1. User Registration
```bash
curl -X POST http://localhost:8080/signup \
```

### 2. User Login
```bash
curl -X POST http://localhost:8080/login \

```

### 3. Create Note (with JWT)
```bash
curl -X POST http://localhost:8080/notes \
```

### 4. Check Note Status
```bash
curl http://localhost:8080/ \
```

## 📊 Monitoring

- **Health Check**: `/actuator/health`
- **Application Info**: `/actuator/info`
- **Docker Logs**: `docker-compose logs -f proksi-app`

## 🎥 Demo

For live demonstration, access the web interface at the root URL which provides:
- User signup/login functionality
- Note creation with real-time AI summarization
- Role-based dashboard views
- Admin panel for viewing all users' data

## 📝 License

MIT License