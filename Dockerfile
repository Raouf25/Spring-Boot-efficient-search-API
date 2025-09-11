# Définir une variable globale pour le fichier JAR
ARG JAR_FILE=spring-boot-efficient-search-api-*.jar

#
# Build stage
#
FROM maven:3.9.11-eclipse-temurin-24-alpine AS maven_build

COPY . /.

RUN mvn clean package -DskipTests

##########################
#
# Step 1: Build a custom JRE
#
FROM eclipse-temurin:24-alpine AS build-jre

# Copy the application JAR file
# Rendre la variable ARG disponible dans ce stage
ARG JAR_FILE
COPY --from=maven_build ./target/${JAR_FILE} /app/app.jar
WORKDIR /app

# Install required tools for jlink
RUN apk add --no-cache binutils

# Unpack the JAR, analyze dependencies, and build a minimal JRE
RUN mkdir -p unpacked && \
    unzip app.jar -d unpacked && \
    $JAVA_HOME/bin/jdeps \
    --ignore-missing-deps \
    --print-module-deps \
    -q \
    --recursive \
    --multi-release 24 \
    --class-path="unpacked/BOOT-INF/lib/*" \
    --module-path="unpacked/BOOT-INF/lib/*" \
    app.jar > /dependencies.info && \
    rm -rf unpacked && \
    $JAVA_HOME/bin/jlink \
         --verbose \
         --add-modules $(cat /dependencies.info) \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=zip-6 \
         --output /customjre

RUN file0="$(cat /dependencies.info)" && echo $file0

##########################
#
# Step 2: Build the main application image
#
FROM alpine:latest

# Set environment variables for the JRE
ENV JAVA_HOME=/jre
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Copy the custom JRE from the previous stage
COPY --from=build-jre /customjre $JAVA_HOME

# Add a non-root user for the application
ARG APPLICATION_USER=appuser
RUN adduser --no-create-home -u 1000 -D $APPLICATION_USER

# Configure the working directory
RUN mkdir /app && chown -R $APPLICATION_USER /app

# Set the working directory
WORKDIR /app

# Copy the application JAR file
ARG JAR_FILE
COPY --chown=1000:1000 --from=maven_build ./target/${JAR_FILE} /app/app.jar
# Expose the application port
EXPOSE 8080

# Define the entrypoint to run the application
ENTRYPOINT [ "java", "-jar", "/app/app.jar" ]


# docker build -t spring-boot-efficient-search-api-0  . -f ./Dockerfile_default

# docker build -t spring-boot-efficient-search-api-1  .
# docker run -t -p 8080:8080  spring-boot-efficient-search-api-1
# docker images --filter=reference='spring-boot-efficient-search-api*' | sort

#  spring-boot-efficient-search-api-0    Started SpringBootEfficientSearchApiApplication in 4.797 seconds (process running for 5.598)
#  spring-boot-efficient-search-api-1    Started SpringBootEfficientSearchApiApplication in 2.243 seconds (process running for 2.547)