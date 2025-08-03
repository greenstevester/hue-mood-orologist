#!/bin/bash

echo "=== Testing Different Schedule Intervals ==="
echo ""

API_KEY="YOUR_API_KEY_HERE"
BRIDGE_IP="10.0.0.101"

echo "Available schedule intervals:"
echo "  MINUTE      - Check every minute (for testing)"
echo "  HOUR        - Check every hour (default)"
echo "  FOUR_HOURS  - Check every 4 hours" 
echo "  TWELVE_HOURS - Check every 12 hours"
echo ""

echo "Example commands:"
echo ""

echo "1. Test with every minute (good for testing):"
echo "./gradlew bootRun --args=\"--hue.api-key=$API_KEY --hue.bridge-ip=$BRIDGE_IP --schedule.interval=MINUTE --hue.target-light-name=Kitchen-Island --hue.target-all-lights=false\""
echo ""

echo "2. Production: Every hour, all lights:"
echo "./gradlew bootRun --args=\"--hue.api-key=$API_KEY --hue.bridge-ip=$BRIDGE_IP --schedule.interval=HOUR\""
echo ""

echo "3. Conservative: Every 4 hours, specific light:"
echo "./gradlew bootRun --args=\"--hue.api-key=$API_KEY --hue.bridge-ip=$BRIDGE_IP --schedule.interval=FOUR_HOURS --hue.target-light-name=bedroom --hue.target-all-lights=false\""
echo ""

echo "4. Minimal: Every 12 hours, all lights:"
echo "./gradlew bootRun --args=\"--hue.api-key=$API_KEY --hue.bridge-ip=$BRIDGE_IP --schedule.interval=TWELVE_HOURS\""
echo ""

echo "To test quickly, run with MINUTE interval and watch the logs!"