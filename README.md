# Proksi - AI-Powered Note Management System

A Spring Boot application with AI-powered text summarization using HuggingFace API, featuring user authentication, role-based access control, and a responsive web interface.

## 🚀 Features

- **User Authentication**: JWT-based authentication with role management (ADMIN/AGENT)
- **AI Text Summarization**: Real-time text summarization using HuggingFace BART model
- **Note Management**: Create, view, and manage notes with AI-generated summaries
- **Admin Dashboard**: Special interface for administrators to view all users' notes
- **Responsive UI**: Modern, responsive web interface built with Thymeleaf
- **PostgreSQL Integration**: Robust database with Flyway migrations
- **Docker Support**: Fully containerized with Docker Compose

## 🛠️ Tech Stack

- **Backend**: Spring Boot 3.5.5, Spring Security, Spring Data JPA
- **Database**: PostgreSQL 16
- **Frontend**: Thymeleaf, HTML5, CSS3, JavaScript
- **AI**: HuggingFace Transformers API (BART-large-CNN)
- **Authentication**: JWT with BCrypt password hashing
- **Migration**: Flyway
- **Containerization**: Docker & Docker Compose
- **Build Tool**: Maven

## 📋 Prerequisites

- Docker and Docker Compose
- Java 21 (for local development)
- Maven 3.9+ (for local development)
- HuggingFace API Token (optional, fallback available)

## 🚀 Quick Start

### Option 1: Docker Deployment (Recommended)

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd proksi
   ```

2. **Configure environment**
   ```bash
   cp .env.example .env
   # Edit .env file with your actual values
   ```

3. **Deploy with Docker**
   ```bash
   ./deploy.sh
   ```

4. **Access the application**
   - Application: http://localhost:8080
   - Database: localhost:5432

### Option 2: Local Development

1. **Setup development environment**
   ```bash
   ./dev-setup.sh
   ```

2. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

## 🔧 Configuration

### Environment Variables

| Variable | Description | Required | Default |
|----------|-------------|----------|---------|
| `DB_PASSWORD` | PostgreSQL password | Yes | - |
| `JWT_SECRET` | JWT signing secret (256+ bits) | Yes | - |
| `JWT_EXPIRATION_MS` | JWT expiration time in ms | No | 86400000 |
| `HUGGINGFACE_API_TOKEN` | HuggingFace API token | No | - |
| `SPRING_PROFILES_ACTIVE` | Spring profile | No | production |

### Getting HuggingFace API Token

1. Sign up at [HuggingFace](https://huggingface.co/)
2. Go to Settings → Access Tokens
3. Create a new token with "Read" permissions
4. Add to your `.env` file

## 📖 Usage

### User Roles

- **AGENT**: Regular users who can create and manage their own notes
- **ADMIN**: Administrators who can view all users' notes and access admin dashboard

### Creating Notes

1. Log in to the application
2. Click "Create New Note"
3. Fill in title and content
4. Save the note
5. Click "AI Processing" to generate AI summary

### Admin Features

- View all users' notes
- Filter notes by status (pending, processing, done, failed)
- Access user management features

## 🐳 Docker Commands

```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# View logs
docker-compose logs -f proksi-app

# Rebuild and restart
docker-compose up --build -d

# Clean up
docker-compose down -v
docker system prune
```

## 🔨 Development

### Local Setup

```bash
# Install dependencies
./mvnw dependency:resolve

# Run tests
./mvnw test

# Build application
./mvnw clean package

# Run with development profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=development
```

### Database Migrations

Flyway migrations are located in `src/main/resources/db/migration/`:

- `V1__create_users_and_notes.sql`: Initial schema
- `V2__fix_constraints.sql`: Constraint updates

## 🚀 Production Deployment

### Docker Compose (Recommended)

```bash
# Production deployment
./deploy.sh
```

### Manual Docker Build

```bash
# Build image
docker build -t proksi-app .

# Run with environment file
docker run --env-file .env -p 8080:8080 proksi-app
```

### Environment-specific Configurations

- **Development**: `application.properties`
- **Production**: `application-production.properties`

## 📊 Monitoring

The application includes Spring Boot Actuator for monitoring:

- Health Check: `/actuator/health`
- Application Info: `/actuator/info`

## 🔒 Security

- JWT-based authentication
- BCrypt password hashing
- CSRF protection
- SQL injection prevention via JPA
- Role-based access control
- Secure headers configuration

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🐛 Troubleshooting

### Common Issues

1. **Database Connection Issues**
   - Ensure PostgreSQL is running
   - Check connection parameters in `.env`

2. **JWT Authentication Problems**
   - Verify JWT_SECRET is set and sufficiently long
   - Check token expiration settings

3. **AI Summarization Not Working**
   - Verify HuggingFace API token
   - Check API rate limits
   - Fallback summary will be used if API fails

4. **Docker Issues**
   - Ensure Docker is running
   - Check port availability (8080, 5432)
   - Review docker-compose logs

### Support

For issues and questions, please create an issue in the repository.