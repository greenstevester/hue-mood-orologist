# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 4.0.0-M1 application named "hue-mood-orologist" designed to interact with Philips Hue lighting systems. The project uses Java 21 and includes GraalVM Native Image support for performance optimization.

## Build Commands

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun

# Run tests
./gradlew test

# Run a single test class
./gradlew test --tests "io.github.greenstevester.hue_mood_orologist.HueMoodOrologistApplicationTests"

# Run a single test method
./gradlew test --tests "io.github.greenstevester.hue_mood_orologist.HueMoodOrologistApplicationTests.contextLoads"

# Clean build artifacts
./gradlew clean

# Build Docker image using Cloud Native Buildpacks
./gradlew bootBuildImage

# Compile to native executable (requires GraalVM 22.3+)
./gradlew nativeCompile

# Run native tests
./gradlew nativeTest

# View all available tasks
./gradlew tasks --all
```

## Architecture

### Package Structure
- Main package: `io.github.greenstevester.hue_mood_orologist` (note: underscores, not hyphens)
- Entry point: `HueMoodOrologistApplication.java` - Standard Spring Boot application class

### Key Dependencies
- Spring Boot REST Client - For HTTP communication with Hue Bridge
- Lombok - Reduces boilerplate code (use `@Data`, `@Builder`, etc.)
- Spring Boot Configuration Processor - For type-safe configuration properties

### Configuration
- Application properties: `src/main/resources/application.properties`
- Spring Boot configuration classes should use `@ConfigurationProperties` for type-safe configuration

### Testing
The project uses JUnit 5 (Jupiter) with Spring Boot Test. Integration tests should extend Spring Boot test annotations:
- `@SpringBootTest` for full integration tests
- `@WebMvcTest` for controller tests
- `@DataJpaTest` for repository tests (when JPA is added)

## Development Notes

1. **REST Client Usage**: Use Spring's new REST Client (not RestTemplate) for HTTP calls to Hue Bridge
2. **Native Compilation**: The project supports GraalVM native compilation. Ensure reflection hints are properly configured for any dynamic features
3. **Lombok**: Already configured - use annotations freely to reduce boilerplate
4. **Package Naming**: Always use underscores (`hue_mood_orologist`) in package names, not hyphens