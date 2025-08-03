#!/bin/bash

echo "=== Testing Hue Light Targeting Feature ==="
echo ""

echo "Test 1: Target ALL lights (default behavior)"
echo "Command: ./gradlew bootRun --args=\"--hue.target-all-lights=true\""
echo ""

echo "Test 2: Target specific light named 'Living Room'"
echo "Command: ./gradlew bootRun --args=\"--hue.target-all-lights=false --hue.target-light-name=Living Room\""
echo ""

echo "Test 3: Target specific light named 'Bedroom Light'"
echo "Command: ./gradlew bootRun --args=\"--hue.target-all-lights=false --hue.target-light-name=Bedroom Light\""
echo ""

echo "Note: Add your API key and bridge IP to the commands above"
echo "Example with credentials:"
echo "./gradlew bootRun --args=\"--hue.api-key=YOUR_API_KEY --hue.bridge-ip=10.0.0.101 --hue.target-all-lights=false --hue.target-light-name=Living Room\""