#!/bin/bash
set -e

export PORT="${PORT:-10000}"
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
JAVA_OPTS="${JAVA_OPTS:--XX:MaxRAMPercentage=70.0 -XX:+UseSerialGC -XX:MaxMetaspaceSize=128m}"
java $JAVA_OPTS -jar /app/app.jar &
JAVA_PID=$!

# Wait for Spring Boot backend to be ready on port 8088 (max 45 seconds)
echo "⏳ Waiting for Spring Boot backend to initialize on port 8088..."
MAX_WAIT=45
WAIT_COUNT=0
while ! (exec 3<>/dev/tcp/127.0.0.1/8088) 2>/dev/null; do
    sleep 1
    WAIT_COUNT=$((WAIT_COUNT + 1))
    if [ "$WAIT_COUNT" -ge "$MAX_WAIT" ]; then
        echo "⚠️ Backend startup took longer than expected, starting Nginx anyway..."
        break
    fi
done
exec 3>&- 2>/dev/null || true
echo "✅ Backend ready or proceeding! Launching Nginx Web Server..."

# Start Nginx web server in foreground
exec nginx -g "daemon off;"
