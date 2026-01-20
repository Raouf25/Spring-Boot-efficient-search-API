# Define global variable for the JAR file
ARG JAR_FILE=spring-boot-efficient-search-api-*.jar
ARG MAVEN_VERSION=3

#
# Build stage
#
FROM maven:${MAVEN_VERSION}-eclipse-temurin-25-alpine AS maven_build

WORKDIR /build

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Cache dependencies layer
RUN mvn dependency:go-offline -B

# Build application
RUN mvn clean package -DskipTests -q

##########################
#
# Step 1: Build a custom JRE
#
FROM eclipse-temurin:25-alpine AS build-jre

WORKDIR /app

# Copy the application JAR file
# Make the ARG variable available in this stage
ARG JAR_FILE
COPY --from=maven_build /build/target/${JAR_FILE} /app/app.jar

# Install required tools for jlink
RUN apk add --no-cache binutils unzip

# Unpack the JAR, analyze dependencies, and build a minimal JRE
RUN mkdir -p unpacked && \
    unzip -q app.jar -d unpacked && \
    $JAVA_HOME/bin/jdeps \
    --ignore-missing-deps \
    --print-module-deps \
    -q \
    --recursive \
    --multi-release 25 \
    --class-path="unpacked/BOOT-INF/lib/*" \
    --module-path="unpacked/BOOT-INF/lib/*" \
    app.jar > /dependencies.info && \
    rm -rf unpacked && \
    $JAVA_HOME/bin/jlink \
         --add-modules $(cat /dependencies.info) \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=zip-6 \
         --output /customjre && \
    cat /dependencies.info

##########################
#
# Step 2: Build the main application image
#
FROM alpine:latest

# Set labels for metadata
LABEL maintainer="Raouf"
LABEL description="Spring Boot Efficient Search API"
LABEL version="1.0"

# Set environment variables for the JRE
ENV JAVA_HOME=/jre \
    PATH="${JAVA_HOME}/bin:${PATH}" \
    LANG=C.UTF-8 \
    TZ=UTC

# Copy the custom JRE from the previous stage
COPY --from=build-jre /customjre /jre/

# Create a non-root user for security
ARG APPLICATION_USER=appuser
ARG APPLICATION_UID=1000
RUN adduser --no-create-home -u ${APPLICATION_UID} -D ${APPLICATION_USER}

# Create application directory
RUN mkdir -p /app && \
    chown -R ${APPLICATION_USER}:${APPLICATION_USER} /app

# Set the working directory
WORKDIR /app

# Switch to non-root user
USER ${APPLICATION_USER}

# Copy the application JAR file
ARG JAR_FILE
COPY --chown=${APPLICATION_UID}:${APPLICATION_UID} --from=maven_build /build/target/${JAR_FILE} /app/app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD /jre/bin/java -cp /app/app.jar org.springframework.boot.loader.JarLauncher 2>/dev/null || exit 1

# Expose the application port
EXPOSE 8080

# Define JVM options for optimization
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:InitiatingHeapOccupancyPercent=35 -XX:+DisableExplicitGC"

# Run the application
ENTRYPOINT [ "/jre/bin/java", "-jar", "/app/app.jar" ]


# docker build -t spring-boot-efficient-search-api-0  . -f ./Dockerfile_default

# docker build -t spring-boot-efficient-search-api-1  .
# docker run -t -p 8080:8080  spring-boot-efficient-search-api-1
# docker images --filter=reference='spring-boot-efficient-search-api*' | sort

#  spring-boot-efficient-search-api-0    Started SpringBootEfficientSearchApiApplication in 4.797 seconds (process running for 5.598)
#  spring-boot-efficient-search-api-1    Started SpringBootEfficientSearchApiApplication in 2.243 seconds (process running for 2.547)
