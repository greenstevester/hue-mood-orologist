# Docker Setup Guide for Hue Mood Orologist

This guide provides detailed steps to build and run the Hue Mood Orologist application using Docker.

## Prerequisites

- Docker installed (version 20.10 or higher)
- Docker Compose installed (version 2.0 or higher)
- Philips Hue Bridge API key
- Network access to your Hue Bridge

## Quick Start

### Option 1: Using the Build Script (Recommended)

```bash
# Basic usage with API key
./docker-build-and-run.sh -k YOUR_API_KEY_HERE

# With bridge IP and debug mode
./docker-build-and-run.sh -k YOUR_API_KEY_HERE -b 192.168.1.100 -d

# Target specific light with custom interval
./docker-build-and-run.sh -k YOUR_API_KEY_HERE -l "Living Room" -i MINUTE
```

### Option 2: Manual Docker Commands

#### Step 1: Build the Docker Image

```bash
# Build using Spring Boot's built-in support
./gradlew bootBuildImage

# OR build using Dockerfile
docker build -t hue-mood-orologist:latest .
```

#### Step 2: Run with Docker

```bash
# Basic run with minimal configuration
docker run -d \
  --name hue-mood-orologist \
  -p 8080:8080 \
  -e HUE_API_KEY=YOUR_API_KEY_HERE \
  hue-mood-orologist:latest

# Advanced run with all options
docker run -d \
  --name hue-mood-orologist \
  -p 8080:8080 \
  -e HUE_API_KEY=YOUR_API_KEY_HERE \
  -e HUE_BRIDGE_IP=192.168.1.100 \
  -e HUE_TARGET_ALL_LIGHTS=false \
  -e HUE_TARGET_LIGHT_NAME="Living Room" \
  -e HUE_DEBUG_COLORS=true \
  -e SCHEDULE_INTERVAL=MINUTE \
  -e COLOR_MAPPING_CONDITIONS_RAIN="light purple" \
  -v $(pwd)/docker-logs:/app/logs \
  --restart unless-stopped \
  hue-mood-orologist:latest
```

### Option 3: Using Docker Compose

#### Step 1: Create Environment File

Create a `.env` file with your configuration:

```env
# Required
HUE_API_KEY=YOUR_API_KEY_HERE

# Optional
HUE_BRIDGE_IP=192.168.1.100
HUE_TARGET_ALL_LIGHTS=true
HUE_TARGET_LIGHT_NAME=
HUE_DEBUG_COLORS=false
SCHEDULE_INTERVAL=HOUR

# Weather thresholds
WEATHER_COLD_THRESHOLD=5.0
WEATHER_RAIN_PROBABILITY_THRESHOLD=30
WEATHER_RAIN_AMOUNT_THRESHOLD=0.1

# Color mappings
COLOR_MAPPING_ENABLED=true
COLOR_MAPPING_CONDITIONS_RAIN=light purple
COLOR_MAPPING_CONDITIONS_SHOWERS=dark purple
COLOR_MAPPING_CONDITIONS_SUNSHINE=yellow
COLOR_MAPPING_CONDITIONS_OVERCAST=grey
COLOR_MAPPING_CONDITIONS_SNOW=white
COLOR_MAPPING_CONDITIONS_COLD=blue
COLOR_MAPPING_CONDITIONS_CLEAR=white
```

#### Step 2: Run with Docker Compose

```bash
# Build and start
docker-compose up -d --build

# View logs
docker-compose logs -f

# Stop
docker-compose down
```

## Environment Variables Reference

### Required Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `HUE_API_KEY` | Your Philips Hue Bridge API key | `5XluL53ccC4jfhghnWrtgK9SacCTUJ6cCijXPIqB` |

### Optional Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `HUE_BRIDGE_IP` | *(auto-discover)* | Bridge IP address |
| `HUE_TARGET_ALL_LIGHTS` | `true` | Control all lights vs specific |
| `HUE_TARGET_LIGHT_NAME` | *(empty)* | Name of specific light |
| `HUE_DEBUG_COLORS` | `false` | Enable color debug mode |
| `SCHEDULE_INTERVAL` | `HOUR` | Check interval (MINUTE, HOUR, FOUR_HOURS, TWELVE_HOURS) |
| `WEATHER_COLD_THRESHOLD` | `5.0` | Temperature threshold for "cold" |
| `WEATHER_RAIN_PROBABILITY_THRESHOLD` | `30` | Rain probability threshold |
| `WEATHER_RAIN_AMOUNT_THRESHOLD` | `0.1` | Rain amount threshold (mm) |
| `COLOR_MAPPING_ENABLED` | `true` | Enable custom color mappings |
| `COLOR_MAPPING_CONDITIONS_*` | *(various)* | Weather-to-color mappings |

## Container Management

### View Container Status

```bash
# Check if container is running
docker ps | grep hue-mood-orologist

# View container details
docker inspect hue-mood-orologist

# Check health status
docker inspect hue-mood-orologist --format='{{.State.Health.Status}}'
```

### View Logs

```bash
# View recent logs
docker logs hue-mood-orologist --tail 50

# Follow logs in real-time
docker logs -f hue-mood-orologist

# View logs with timestamps
docker logs -t hue-mood-orologist
```

### Container Shell Access

```bash
# Access container shell
docker exec -it hue-mood-orologist /bin/sh

# View application properties
docker exec hue-mood-orologist cat /app/application.properties
```

### Restart and Stop

```bash
# Restart container
docker restart hue-mood-orologist

# Stop container
docker stop hue-mood-orologist

# Remove container
docker rm hue-mood-orologist
```

## Docker Image Details

### Multi-Stage Build

The Dockerfile uses a multi-stage build for efficiency:

1. **Build Stage**: Uses Gradle to compile the application
2. **Runtime Stage**: Uses minimal JRE Alpine image

### Security Features

- Runs as non-root user (`spring:spring`)
- Health check endpoint configured
- Resource limits applied
- Minimal base image (Alpine Linux)

### Image Size

```bash
# Check image size
docker images hue-mood-orologist

# Expected size: ~200-250MB (JRE Alpine based)
```

## Troubleshooting

### Container Won't Start

```bash
# Check logs for errors
docker logs hue-mood-orologist

# Common issues:
# - Missing HUE_API_KEY environment variable
# - Network connectivity to Hue Bridge
# - Port 8080 already in use
```

### Bridge Discovery Issues

If auto-discovery fails in Docker:

1. Provide bridge IP explicitly: `-e HUE_BRIDGE_IP=192.168.1.100`
2. Ensure container can reach your local network
3. Use host network mode: `--network host` (Linux only)

### Permission Issues

```bash
# Fix log directory permissions
sudo chown -R 1000:1000 ./docker-logs

# Or run container as root (not recommended)
docker run --user root ...
```

### Memory Issues

```bash
# Increase memory limits in docker-compose.yml
deploy:
  resources:
    limits:
      memory: 1G
    reservations:
      memory: 768M
```

## Advanced Configuration

### Custom Network

```yaml
# docker-compose.yml
networks:
  hue-network:
    driver: bridge
    ipam:
      config:
        - subnet: 172.20.0.0/16
```

### Persistent Configuration

```bash
# Mount custom application.properties
docker run -v $(pwd)/custom-app.properties:/app/application.properties ...
```

### Using Secrets

```yaml
# docker-compose with secrets
secrets:
  hue_api_key:
    file: ./secrets/hue_api_key.txt

services:
  hue-mood-orologist:
    secrets:
      - hue_api_key
    environment:
      - HUE_API_KEY_FILE=/run/secrets/hue_api_key
```

## Native Image with GraalVM

For faster startup and lower memory usage:

```bash
# Build native image
./gradlew nativeCompile

# Build Docker image with native binary
docker build -f Dockerfile.native -t hue-mood-orologist:native .
```

## Monitoring

### Prometheus Metrics

```yaml
# Add to docker-compose.yml
ports:
  - "8080:8080"  # Application
  - "8081:8081"  # Actuator/Metrics
```

### Health Check

```bash
# Manual health check
curl http://localhost:8080/actuator/health
```

## Best Practices

1. **Always use .env files** for sensitive configuration
2. **Set resource limits** to prevent container from consuming too much memory
3. **Use named volumes** for persistent data
4. **Enable health checks** for automatic container recovery
5. **Use specific image tags** in production (not `latest`)
6. **Monitor logs** regularly for errors
7. **Keep images updated** with security patches

## Example Production Setup

```yaml
# docker-compose.prod.yml
version: '3.8'

services:
  hue-mood-orologist:
    image: hue-mood-orologist:1.0.0
    container_name: hue-mood-orologist-prod
    ports:
      - "8080:8080"
    env_file:
      - .env.prod
    volumes:
      - ./logs:/app/logs
      - ./config:/app/config:ro
    restart: always
    deploy:
      resources:
        limits:
          memory: 512M
          cpus: '0.5'
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
    healthcheck:
      test: ["CMD", "wget", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
```

## Support

For issues specific to Docker setup:
1. Check container logs: `docker logs hue-mood-orologist`
2. Verify environment variables: `docker exec hue-mood-orologist env`
3. Test network connectivity: `docker exec hue-mood-orologist ping YOUR_BRIDGE_IP`
4. Review this guide's troubleshooting section