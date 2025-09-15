#!/bin/bash

# Build and Deploy Script for Proksi Application
set -e

echo "🚀 Starting Proksi Application Deployment..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Functions
print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker and try again."
    exit 1
fi

print_success "Docker is running"

# Check if .env file exists
if [ ! -f .env ]; then
    print_warning ".env file not found. Creating from .env.example..."
    if [ -f .env.example ]; then
        cp .env.example .env
        print_warning "Please edit .env file with your actual values before running again."
        exit 1
    else
        print_error ".env.example file not found. Please create .env file manually."
        exit 1
    fi
fi

print_success ".env file found"

# Load environment variables
source .env

# Validate required environment variables
if [ -z "$DB_PASSWORD" ] || [ "$DB_PASSWORD" = "your_secure_postgres_password_here" ]; then
    print_error "Please set DB_PASSWORD in .env file"
    exit 1
fi

if [ -z "$JWT_SECRET" ] || [ "$JWT_SECRET" = "your_super_secret_jwt_key_at_least_256_bits_long_for_security" ]; then
    print_error "Please set JWT_SECRET in .env file"
    exit 1
fi

print_success "Environment variables validated"

# Stop existing containers
echo "🛑 Stopping existing containers..."
docker-compose down

# Clean up old images (optional)
read -p "Do you want to remove old Docker images? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "🧹 Cleaning up old images..."
    docker system prune -f
    print_success "Old images cleaned up"
fi

# Build and start services
echo "🔨 Building and starting services..."
docker-compose up --build -d

# Wait for services to be healthy
echo "⏳ Waiting for services to be healthy..."
timeout=120
counter=0

while [ $counter -lt $timeout ]; do
    if docker-compose ps | grep -q "healthy"; then
        print_success "All services are healthy!"
        break
    fi
    
    if [ $counter -eq $timeout ]; then
        print_error "Services did not become healthy within $timeout seconds"
        echo "📋 Current service status:"
        docker-compose ps
        exit 1
    fi
    
    sleep 5
    counter=$((counter + 5))
    echo "Waiting... ($counter/$timeout seconds)"
done

# Show service status
echo "📋 Service status:"
docker-compose ps

# Show application logs
echo "📝 Recent application logs:"
docker-compose logs --tail=20 proksi-app

print_success "Deployment completed successfully!"
echo ""
echo "🌐 Application is running at: http://localhost:8080"
echo "🗃️  Database is running at: localhost:5432"
echo ""
echo "📖 Useful commands:"
echo "  - View logs: docker-compose logs -f proksi-app"
echo "  - Stop services: docker-compose down"
echo "  - Restart: docker-compose restart"
echo "  - Update: ./deploy.sh"