#!/bin/bash

# Development Setup Script
set -e

echo "🔧 Setting up Proksi Application for Development..."

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

# Create .env file for development if it doesn't exist
if [ ! -f .env ]; then
    print_warning "Creating development .env file..."
    cat > .env << EOF
# Development Environment Variables
DB_PASSWORD=proksi123
JWT_SECRET=development_jwt_secret_key_for_testing_only_not_for_production_use
JWT_EXPIRATION_MS=86400000
HUGGINGFACE_API_TOKEN=
SPRING_PROFILES_ACTIVE=development
SERVER_PORT=8080
POSTGRES_USER=postgres
POSTGRES_DB=pdb
EOF
    print_success "Development .env file created"
fi

# Start only PostgreSQL for development
echo "🐘 Starting PostgreSQL for development..."
docker-compose up -d postgres

echo "⏳ Waiting for PostgreSQL to be ready..."
sleep 10

# Check if PostgreSQL is ready
until docker-compose exec postgres pg_isready -U postgres -d pdb; do
    echo "Waiting for PostgreSQL..."
    sleep 2
done

print_success "PostgreSQL is ready!"

echo ""
echo "🔧 Development environment is ready!"
echo ""
echo "📖 Next steps:"
echo "  1. Set your HuggingFace API token in .env file"
echo "  2. Run the application with: ./mvnw spring-boot:run"
echo "  3. Application will be available at: http://localhost:8080"
echo ""
echo "🗃️  Database connection details:"
echo "  Host: localhost"
echo "  Port: 5432"
echo "  Database: pdb"
echo "  Username: postgres"
echo "  Password: proksi123"