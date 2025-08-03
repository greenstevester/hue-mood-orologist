#!/bin/bash

echo "==================================="
echo "Hue Mood Orologist Docker Setup"
echo "==================================="
echo ""

# Function to display usage
usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -k, --api-key KEY        Set Hue API key"
    echo "  -b, --bridge-ip IP       Set Hue bridge IP address"
    echo "  -l, --light-name NAME    Target specific light by name"
    echo "  -d, --debug              Enable debug mode"
    echo "  -i, --interval INTERVAL  Set schedule interval (MINUTE|HOUR|FOUR_HOURS|TWELVE_HOURS)"
    echo "  -h, --help               Display this help message"
    echo ""
    echo "Examples:"
    echo "  $0 -k YOUR_API_KEY -b 192.168.1.100"
    echo "  $0 -k YOUR_API_KEY -l \"Living Room\" -d"
    echo "  $0 -k YOUR_API_KEY -i MINUTE --debug"
    echo ""
}

# Parse command line arguments
API_KEY=""
BRIDGE_IP=""
LIGHT_NAME=""
DEBUG_MODE="false"
INTERVAL="HOUR"
TARGET_ALL="true"

while [[ $# -gt 0 ]]; do
    case $1 in
        -k|--api-key)
            API_KEY="$2"
            shift 2
            ;;
        -b|--bridge-ip)
            BRIDGE_IP="$2"
            shift 2
            ;;
        -l|--light-name)
            LIGHT_NAME="$2"
            TARGET_ALL="false"
            shift 2
            ;;
        -d|--debug)
            DEBUG_MODE="true"
            shift
            ;;
        -i|--interval)
            INTERVAL="$2"
            shift 2
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            usage
            exit 1
            ;;
    esac
done

# Check if API key is provided
if [ -z "$API_KEY" ]; then
    echo "Error: Hue API key is required!"
    echo ""
    usage
    exit 1
fi

# Create .env file for docker-compose
echo "Creating .env file with configuration..."
cat > .env << EOF
# Hue Configuration
HUE_API_KEY=$API_KEY
HUE_BRIDGE_IP=$BRIDGE_IP
HUE_TARGET_ALL_LIGHTS=$TARGET_ALL
HUE_TARGET_LIGHT_NAME=$LIGHT_NAME
HUE_DEBUG_COLORS=$DEBUG_MODE

# Schedule Configuration
SCHEDULE_INTERVAL=$INTERVAL

# Weather Configuration (defaults)
WEATHER_COLD_THRESHOLD=5.0
WEATHER_RAIN_PROBABILITY_THRESHOLD=30
WEATHER_RAIN_AMOUNT_THRESHOLD=0.1

# Color Mapping Configuration
COLOR_MAPPING_ENABLED=true
COLOR_MAPPING_CONDITIONS_RAIN=light purple
COLOR_MAPPING_CONDITIONS_SHOWERS=dark purple
COLOR_MAPPING_CONDITIONS_SUNSHINE=yellow
COLOR_MAPPING_CONDITIONS_OVERCAST=grey
COLOR_MAPPING_CONDITIONS_SNOW=white
COLOR_MAPPING_CONDITIONS_COLD=blue
COLOR_MAPPING_CONDITIONS_CLEAR=white
EOF

echo "Configuration saved to .env file"
echo ""

# Build Docker image
echo "Step 1: Building Docker image..."
echo "================================"
docker build -t hue-mood-orologist:latest . || {
    echo "Error: Docker build failed!"
    exit 1
}
echo "✅ Docker image built successfully!"
echo ""

# Create logs directory
mkdir -p docker-logs

# Stop any existing container
echo "Step 2: Stopping existing container (if any)..."
docker-compose down 2>/dev/null

# Run with docker-compose
echo ""
echo "Step 3: Starting application with Docker Compose..."
echo "==================================================="
docker-compose up -d || {
    echo "Error: Failed to start container!"
    exit 1
}

echo ""
echo "✅ Application started successfully!"
echo ""
echo "Container Information:"
echo "======================"
docker ps --filter "name=hue-mood-orologist" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo ""

# Display logs
echo "Viewing application logs (Ctrl+C to exit):"
echo "=========================================="
echo ""
docker-compose logs -f --tail=50