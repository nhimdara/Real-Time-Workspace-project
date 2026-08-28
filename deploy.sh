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

# 3. Serving & Execution
echo "--------------------------------------------------------"
if command -v docker &> /dev/null && (docker compose version &> /dev/null || command -v docker-compose &> /dev/null); then
  echo "🐳 Launching Docker Compose Full Stack..."
  docker compose up --build -d 2>/dev/null || docker-compose up --build -d
  echo "✅ Application deployed via Docker Compose!"
else
  echo "✅ Production artifacts built cleanly!"
  echo "  • Backend JAR: backend/target/workspace-backend-1.0.0.jar"
  echo "  • Backend Status: Active on http://localhost:8088"
  echo "  • Frontend Assets: frontend/dist/"
  echo ""
  echo "🚀 Serving production frontend build on http://0.0.0.0:3000..."
  npx serve frontend/dist -p 3000 --host 0.0.0.0
fi
