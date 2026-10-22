# Stage 1: Build the application using Maven
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# Copy Maven wrapper and POM file
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Normalize line endings and ensure execution permissions for wrapper
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Download dependencies to leverage Docker cache
RUN ./mvnw dependency:go-offline -B || true

# Copy project source code
COPY src ./src

# Build production executable JAR (skipping unit tests during image build)
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Minimal runtime environment
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Create a non-root system user for security
RUN useradd -r -u 1001 -g root appuser

# Copy the built JAR artifact from the builder stage
COPY --from=builder --chown=appuser:root /app/target/smartqueue-0.0.1-SNAPSHOT.jar app.jar

USER appuser

# Render dynamically sets PORT; default to 8080 locally
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
