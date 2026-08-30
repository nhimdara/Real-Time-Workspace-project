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
# Cap the heap so the JVM fits within small containers (e.g. Render free tier = 512MB).
# Without this the backend can be OOM-killed, leaving nginx to return 502 on /api
# (which surfaces in the UI as "registration failed" / "login failed").
JAVA_OPTS="${JAVA_OPTS:--XX:MaxRAMPercentage=70.0 -XX:+UseSerialGC -XX:MaxMetaspaceSize=128m}"
java $JAVA_OPTS -jar /app/app.jar &
JAVA_PID=$!

# Start Nginx web server in foreground
echo "🌐 Launching Nginx Web Server..."
exec nginx -g "daemon off;"
