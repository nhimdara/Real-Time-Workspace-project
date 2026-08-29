#!/bin/bash
set -e

export PORT="${PORT:-80}"
echo "🚀 Starting Real-Time Workspace Platform on PORT=${PORT}..."

# Substitute $PORT variable into Nginx configuration
envsubst '${PORT}' < /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf

# Trap termination signals for clean process exit
cleanup() {
    echo "Stopping services..."
    if [ -n "$JAVA_PID" ]; then
        kill -TERM "$JAVA_PID" 2>/dev/null || true
    fi
    exit 0
}
trap cleanup SIGTERM SIGINT

# Start Java Spring Boot backend in background
echo "☕ Launching Spring Boot Backend Service..."
java -jar /app/app.jar &
JAVA_PID=$!

# Start Nginx web server in foreground
echo "🌐 Launching Nginx Web Server..."
exec nginx -g "daemon off;"
