#!/bin/bash
set -e

echo "🚀 Starting Production Build & Deployment..."

# 1. Build Backend Executable JAR
echo "📦 Building Spring Boot Backend..."
cd backend
mvn clean package -DskipTests
cd ..

# 2. Build Frontend Production Assets
echo "🎨 Building Vue 3 Frontend..."
cd frontend
npm run build
cd ..

# 3. Check Docker status and launch full stack
if command -v docker-compose &> /dev/null || docker compose version &> /dev/null; then
  echo "🐳 Launching Docker Compose Full Stack..."
  docker-compose up --build -d || docker compose up --build -d
  echo "✅ Application deployed successfully via Docker Compose!"
else
  echo "✅ Production artifacts built cleanly!"
  echo "  - Backend JAR: backend/target/workspace-backend-1.0.0.jar"
  echo "  - Frontend Assets: frontend/dist/"
fi

echo "✨ Deployment Ready!"
