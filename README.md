# Hue Mood Orologist

Automatically sync your Philips Hue lights with weather conditions. Rain makes lights blue/grey, cold weather creates warm colors, and normal weather uses standard lighting.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0--M1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-BSD%203--Clause-blue.svg)](LICENSE)

## Features

- 🌦️ **Automatic weather monitoring** using Open-Meteo API (Zurich, Switzerland)
- 💡 **Smart light control** based on weather conditions
- 🎨 **Configurable weather-to-color mapping** - define custom colors for each weather condition
- ⏰ **Configurable check intervals** (every minute to every 12 hours)
- 🎯 **Target specific lights** or control all lights at once
- 🔧 **Customizable weather thresholds** for cold and rain detection
- 🔍 **Auto-discovery** of Philips Hue Bridge on your network
- 🛠️ **GraalVM Native Image** support for fast startup and low memory usage

## Quick Start

### Prerequisites

- Java 21 or higher
- Philips Hue Bridge connected to your network
- Internet connection for weather data

### 1. Get Your Hue Bridge API Key

Press the button on your Hue bridge, then run:

```bash
git clone https://github.com/your-username/hue-mood-orologist.git
cd hue-mood-orologist
./gradlew bootRun
```

Follow the console instructions to get your API key. The application will discover your bridge and provide registration instructions.

### 2. Configure the Application

Create or edit `src/main/resources/application.properties`:

```properties
# Your Hue Bridge API key (required)
hue.api-key=YOUR_API_KEY_HERE

# Bridge IP (optional - leave empty for auto-discovery)
hue.bridge-ip=10.0.0.101

# Schedule - check weather every hour
schedule.interval=HOUR
```

### 3. Run the Application

```bash
./gradlew bootRun
```

The application will:
1. Connect to your Hue bridge
2. List available lights
3. Start weather monitoring
4. Update light colors based on weather conditions

## Weather to Light Mapping

The application detects specific weather conditions and maps them to customizable colors:

| Weather Condition | Default Color | Description |
|------------------|---------------|-------------|
| **Rain** ☔ | Light purple | Light precipitation detected |
| **Showers** 🌧️ | Dark purple | Heavy precipitation/storms |
| **Snow** ❄️ | White | Freezing precipitation |
| **Sunshine** ☀️ | Yellow | Clear, warm weather |
| **Overcast** ☁️ | Grey | Cloudy but no precipitation |
| **Cold** 🥶 | Warm yellow | Very cold but dry |
| **Clear** 🌤️ | Cool white | Normal clear weather |

### Customizable Color Mapping

You can define your own weather-to-color mappings in `application.properties`:

```properties
# Enable color mapping feature
color-mapping.enabled=true

# Define weather condition colors
color-mapping.conditions.rain=light purple
color-mapping.conditions.showers=dark purple  
color-mapping.conditions.sunshine=yellow
color-mapping.conditions.overcast=grey
color-mapping.conditions.snow=white
color-mapping.conditions.cold=warm yellow
color-mapping.conditions.clear=cool white

# Default color for unmapped conditions
color-mapping.default-color=white
```

**Supported Color Formats:**
- **Named colors**: `red`, `blue`, `light purple`, `warm yellow`, `storm grey`
- **Hex colors**: `#FF0000`, `#00FF00`, `#FFFFFF`
- **RGB values**: `255,0,0`, `0,255,0`, `128,128,128`

*Current weather monitoring location: Zurich, Switzerland (47.3769°N, 8.5417°E)*

## Configuration Options

### Weather Settings

| Property | Default | Description |
|----------|---------|-------------|
| `weather.cold.threshold` | `5.0` | Temperature (°C) below which weather is considered "cold" |
| `weather.rain.probability.threshold` | `30` | Precipitation probability (%) threshold for "rain" |
| `weather.rain.amount.threshold` | `0.1` | Precipitation amount (mm) threshold for "rain" |

### Schedule Settings

| Property | Default | Options | Description |
|----------|---------|---------|-------------|
| `schedule.interval` | `HOUR` | `MINUTE`, `HOUR`, `FOUR_HOURS`, `TWELVE_HOURS` | How often to check weather |
| `schedule.initial-delay-seconds` | `5` | Any integer | Delay before first weather check |

### Hue Bridge Settings

| Property | Default | Description |
|----------|---------|-------------|
| `hue.api-key` | *(empty)* | Your Hue bridge API key (required) |
| `hue.bridge-ip` | *(empty)* | Bridge IP address (leave empty for auto-discovery) |
| `hue.target-all-lights` | `true` | Control all lights vs specific light |
| `hue.target-light-name` | *(empty)* | Name of specific light to control |
| `hue.app-name` | `HueMoodOrologist` | Application name registered with bridge |
| `hue.auto-discover-bridge` | `true` | Enable automatic bridge discovery |
| `hue.discovery-timeout` | `10` | Timeout (seconds) for bridge discovery |
| `hue.debug-colors` | `false` | Enable color debug mode on startup |

### Color Mapping Settings

| Property | Default | Description |
|----------|---------|-------------|
| `color-mapping.enabled` | `true` | Enable weather-to-color mapping feature |
| `color-mapping.default-color` | `white` | Default color for unmapped conditions |
| `color-mapping.conditions.*` | *(see examples)* | Weather condition to color mappings |

## Usage Examples

### Testing Setup (Every Minute)
Perfect for testing and watching immediate light changes:

```bash
./gradlew bootRun --args="--hue.api-key=YOUR_API_KEY --schedule.interval=MINUTE --hue.target-light-name=Kitchen --hue.target-all-lights=false"
```

### Production Setup (Every Hour, All Lights)
Recommended for daily use:

```bash
./gradlew bootRun --args="--hue.api-key=YOUR_API_KEY --schedule.interval=HOUR"
```

### Conservative Setup (Every 4 Hours, Specific Light)
For minimal API usage:

```bash
./gradlew bootRun --args="--hue.api-key=YOUR_API_KEY --schedule.interval=FOUR_HOURS --hue.target-light-name=Bedroom --hue.target-all-lights=false"
```

### Cold Climate Setup
For locations with colder weather:

```bash
./gradlew bootRun --args="--hue.api-key=YOUR_API_KEY --weather.cold.threshold=0.0 --weather.rain.probability.threshold=20"
```

### Custom Color Mapping Setup
Use your own weather-to-color preferences:

```bash
./gradlew bootRun --args="--hue.api-key=YOUR_API_KEY --color-mapping.conditions.rain=#4B0082 --color-mapping.conditions.sunshine=255,215,0 --color-mapping.conditions.snow=snow white"
```

### Disable Color Mapping (Legacy Mode)
Use the original rain/cold logic instead of color mapping:

```bash
./gradlew bootRun --args="--hue.api-key=YOUR_API_KEY --color-mapping.enabled=false"
```

### Debug Current Light Colors
Enable debug mode to inspect current light colors and RGB settings:

```bash
./gradlew bootRun --args="--hue.api-key=YOUR_API_KEY --hue.debug-colors=true"
```

This displays:
- Current power state (ON/OFF) for each light
- Light names and IDs  
- Available color information from the Hue API
- Raw API endpoints for manual inspection with curl
- Troubleshooting guidance for color mapping issues

## Running Scripts

The project includes convenient shell scripts:

```bash
# Run with examples and configuration help
./gradleBootRun.sh

# Test different schedule intervals
./test-schedules.sh

# Test light targeting options
./test-targeting.sh

# Test color debug feature
./test-color-debug.sh
```

## Sample Configuration

Complete `application.properties` example:

```properties
spring.application.name=hue-mood-orologist

# Weather API Configuration
weather.api.url=https://api.open-meteo.com/v1/forecast
weather.cold.threshold=8.0
weather.rain.probability.threshold=25
weather.rain.amount.threshold=0.15

# Hue Bridge Configuration
hue.api-key=YOUR_API_KEY_HERE
hue.bridge-ip=192.168.1.100
hue.target-all-lights=false
hue.target-light-name=Living Room
hue.app-name=HueMoodOrologist
hue.auto-discover-bridge=true
hue.discovery-timeout=10
hue.debug-colors=false

# Schedule Configuration
schedule.interval=HOUR
schedule.initial-delay-seconds=5

# Color Mapping Configuration
color-mapping.enabled=true
color-mapping.default-color=white
color-mapping.conditions.rain=light purple
color-mapping.conditions.showers=dark purple
color-mapping.conditions.sunshine=yellow
color-mapping.conditions.overcast=grey
color-mapping.conditions.snow=white
color-mapping.conditions.cold=warm yellow
color-mapping.conditions.clear=cool white

# Logging
logging.level.io.github.greenstevester.hue_mood_orologist=INFO
```

## Building and Development

### Build the Application

```bash
./gradlew build
```

### Run Tests

```bash
./gradlew test
```

### Create Native Image (GraalVM)

```bash
./gradlew nativeCompile
./build/native/nativeCompile/hue-mood-orologist
```

### Create Docker Image

```bash
# Quick start with Docker
./docker-build-and-run.sh -k YOUR_API_KEY_HERE -b YOUR_BRIDGE_IP

# Manual Docker build
./gradlew bootBuildImage
# OR
docker build -t hue-mood-orologist:latest .

# Run with Docker
docker run -d \
  --name hue-mood-orologist \
  -p 8080:8080 \
  -e HUE_API_KEY=YOUR_API_KEY_HERE \
  -e HUE_BRIDGE_IP=192.168.1.100 \
  hue-mood-orologist:latest

# Using Docker Compose
docker-compose up -d
```

For detailed Docker setup instructions, see [DOCKER.md](DOCKER.md).

## Troubleshooting

### Bridge Not Found
**Error:** `No Hue bridges found on the network`

**Solutions:**
1. Ensure bridge is powered on and connected to your network
2. Try manual IP configuration: `hue.bridge-ip=192.168.1.XXX`
3. Check firewall settings allowing multicast discovery
4. Verify you're on the same network as the bridge

### API Key Issues
**Error:** `Hue API key not configured`

**Solutions:**
1. Press the physical button on your Hue bridge
2. Run the application within 30 seconds
3. Copy the API key from console output to your configuration
4. Ensure the API key is exactly as provided (no extra spaces)

### Target Light Not Found
**Error:** `Target light 'X' not found`

**Solutions:**
1. Check exact light name in the Philips Hue app
2. Names are case-insensitive but must match exactly
3. Use `hue.target-all-lights=true` to target all lights instead
4. Run the application to see all available light names

### Weather API Issues
**Error:** `Error fetching weather data`

**Solutions:**
1. Check internet connectivity
2. Verify Open-Meteo API is accessible
3. Check firewall allows outbound HTTPS connections
4. Try manually accessing: https://api.open-meteo.com/v1/forecast?latitude=47.3769&longitude=8.5417&hourly=temperature_2m

### Application Won't Start
**Error:** Various startup errors

**Solutions:**
1. Ensure Java 21 or higher is installed: `java --version`
2. Run `./gradlew clean build` to rebuild
3. Check `application.properties` syntax
4. Verify all required properties are set

## How It Works

1. **Weather Monitoring**: The application queries the Open-Meteo API every configured interval
2. **Condition Analysis**: Weather data is analyzed to determine specific conditions:
   - **Snow**: Cold temperature + precipitation
   - **Rain/Showers**: Precipitation detected (light vs heavy)
   - **Sunshine**: High temperature, clear skies
   - **Overcast**: Moderate precipitation probability
   - **Cold/Clear**: Based on temperature thresholds
3. **Color Mapping**: Weather conditions are mapped to colors using:
   - **Configurable mappings** from `application.properties` (preferred)
   - **Legacy logic** for rain/cold detection (fallback)
4. **Light Control**: Colors are applied to targeted lights using:
   - **Named colors** (e.g., "light purple", "warm yellow")
   - **Hex values** (e.g., "#FF0000", "#4B0082")
   - **RGB tuples** (e.g., "255,0,0", "128,128,128")
5. **Smart Targeting**: Lights can be controlled individually or as a group
6. **Reliable Scheduling**: Uses Spring's TaskScheduler for precise timing

## Location Customization

To monitor weather for a different location, modify the `WeatherService.java`:

```java
String url = apiBaseUrl + 
    "?latitude=YOUR_LATITUDE&longitude=YOUR_LONGITUDE" +
    "&hourly=temperature_2m,precipitation_probability,precipitation" +
    "&timezone=YOUR_TIMEZONE&forecast_hours=8";
```

Popular locations:
- London: `latitude=51.5074&longitude=-0.1278&timezone=Europe/London`
- New York: `latitude=40.7128&longitude=-74.0060&timezone=America/New_York`
- Tokyo: `latitude=35.6762&longitude=139.6503&timezone=Asia/Tokyo`

## Dependencies

- **Spring Boot 4.0.0-M1** - Application framework
- **Huevana 4.0.3** - Philips Hue control library
- **Open-Meteo API** - Weather data source
- **Lombok** - Code generation
- **JUnit 5** - Testing framework

## License

This project is licensed under the BSD 3-Clause License - see the [LICENSE](LICENSE) file for details.

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Support

- 📚 Check the [troubleshooting guide](#troubleshooting) above
- 🐛 Report bugs via [GitHub Issues](https://github.com/your-username/hue-mood-orologist/issues)
- 💡 Request features via [GitHub Issues](https://github.com/your-username/hue-mood-orologist/issues)
- 📖 Review the [Philips Hue Developer Documentation](https://developers.meethue.com/)

---

*Weather data provided by [Open-Meteo](https://open-meteo.com/) - a free weather API.*