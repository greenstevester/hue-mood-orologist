#!/bin/bash

# Run the Hue Mood Orologist Spring Boot application

echo "Starting Hue Mood Orologist..."
echo "Configuration options:"
echo "  --hue.target-all-lights=true|false    (default: true)"
echo "  --hue.target-light-name='Light Name'  (only if target-all-lights=false)"
echo "  --schedule.interval=MINUTE|HOUR|FOUR_HOURS|TWELVE_HOURS  (default: HOUR)"
echo "  --schedule.initial-delay-seconds=N     (default: 5)"
echo ""

# Default: Target all lights
#./gradlew bootRun --args="--hue.api-key=YOUR_API_KEY_HERE --hue.bridge-ip=10.0.0.101"

# Examples for different configurations:
# 
# Target specific light with every minute schedule (for testing):
# ./gradlew bootRun --args="--hue.api-key=YOUR_API_KEY_HERE --hue.bridge-ip=10.0.0.101 --hue.target-all-lights=false --hue.target-light-name=Kitchen-Island --schedule.interval=MINUTE"
#
# Run every 4 hours targeting all lights:
# ./gradlew bootRun --args="--hue.api-key=YOUR_API_KEY_HERE --hue.bridge-ip=10.0.0.101 --schedule.interval=FOUR_HOURS"
#
# Run every 12 hours with 30 second initial delay:
# ./gradlew bootRun --args="--hue.api-key=YOUR_API_KEY_HERE --hue.bridge-ip=10.0.0.101 --schedule.interval=TWELVE_HOURS --schedule.initial-delay-seconds=30"

# Current configuration - targeting Kitchen-Island every minute for testing
./gradlew bootRun --args="--hue.api-key=YOUR_API_KEY_HERE --hue.bridge-ip=10.0.0.101 --hue.target-all-lights=false --hue.target-light-name=Kitchen-Island --schedule.interval=MINUTE"