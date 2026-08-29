# Multi-Stage Full-Stack Dockerfile for Real-Time Workspace Platform
# ---------------------------------------------------------------

# Stage 1: Build Vue 3 Frontend
FROM node:20-alpine AS frontend-builder
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# Stage 2: Build Spring Boot Backend
FROM maven:3.9.6-eclipse-temurin-17-alpine AS backend-builder
WORKDIR /app/backend
COPY backend/pom.xml ./
COPY backend/src ./src
RUN mvn clean package -DskipTests

# Stage 3: Final Unified Production Container
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache nginx gettext bash

WORKDIR /app

# Copy built frontend assets
COPY --from=frontend-builder /app/frontend/dist /usr/share/nginx/html

# Copy built backend JAR
COPY --from=backend-builder /app/backend/target/workspace-backend-1.0.0.jar app.jar

# Copy Nginx template configuration & startup entrypoint script
COPY nginx.conf /etc/nginx/nginx.conf.template
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Expose ports (Default HTTP 80, Backend 8088, Render dynamic 10000)
EXPOSE 80 8088 10000

ENTRYPOINT ["/entrypoint.sh"]
