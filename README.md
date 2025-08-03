# Hue Mood Orologist

Automatically sync your Philips Hue lights with weather conditions. Rain makes lights blue/grey, cold weather creates warm colors, and normal weather uses standard lighting.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0--M1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-BSD%203--Clause-blue.svg)](LICENSE)

## Features

- 🌦️ **Automatic weather monitoring** using Open-Meteo API (Zurich, Switzerland)
- 💡 **Smart light control** based on weather conditions
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

| Weather Condition | Light Behavior | Description |
|------------------|----------------|-------------|
| **Rain** ☔ | Blue/grey mood | Cool colors to reflect rainy weather |
| **Cold** ❄️ | Warm orange/yellow | Warm colors for temperatures below threshold |
| **Normal** ☀️ | Standard white light | Normal lighting for good weather |

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

## Running Scripts

The project includes convenient shell scripts:

```bash
# Run with examples and configuration help
./gradleBootRun.sh

# Test different schedule intervals
./test-schedules.sh

# Test light targeting options
./test-targeting.sh
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
hue.api-key=5XluL53ccC4jfhghnWrtgK9SacCTUJ6cCijXPIqB
hue.bridge-ip=192.168.1.100
hue.target-all-lights=false
hue.target-light-name=Living Room
hue.app-name=HueMoodOrologist
hue.auto-discover-bridge=true
hue.discovery-timeout=10

# Schedule Configuration
schedule.interval=HOUR
schedule.initial-delay-seconds=5

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
./gradlew bootBuildImage
docker run --rm hue-mood-orologist:0.0.1-SNAPSHOT
```

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
2. **Condition Analysis**: Weather data is analyzed for rain probability/amount and temperature
3. **Light Control**: Based on conditions, appropriate light colors are applied:
   - Rain detected → Blue/grey colors
   - Cold temperature → Warm orange/yellow colors  
   - Normal conditions → Standard white lighting
4. **Smart Targeting**: Lights can be controlled individually or as a group
5. **Reliable Scheduling**: Uses Spring's TaskScheduler for precise timing

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