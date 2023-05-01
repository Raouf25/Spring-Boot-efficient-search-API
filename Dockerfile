#
# Build stage
#
FROM maven:3.8.7-eclipse-temurin-19 AS MAVEN_BUILD
COPY pom.xml /build/
COPY . /build/
WORKDIR /build/
#The option "--quiet" is used to limit the output to only warnings and errors
RUN mvn -f /build/pom.xml clean package --quiet

#
# Package stage
#
# base image to build a JRE
FROM amazoncorretto:19.0.2-alpine as corretto-jdk

# required for strip-debug to work
RUN apk add --no-cache binutils

# Build small JRE image
RUN $JAVA_HOME/bin/jlink \
         --verbose \
         --add-modules ALL-MODULE-PATH \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /customjre

# main app image
FROM alpine:latest
ENV JAVA_HOME=/jre
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# copy JRE from the base image
COPY --from=corretto-jdk /customjre $JAVA_HOME

# Add app user
ARG APPLICATION_USER=appuser
RUN adduser --no-create-home -u 1000 -D $APPLICATION_USER

# Configure working directory
RUN mkdir /app && \
    chown -R $APPLICATION_USER /app

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --chown=1000:1000  --from=MAVEN_BUILD /build/target/spring-boot-efficient-search-api-0.0.1-SNAPSHOT.jar  /app/app.jar

# Expose port 8080
EXPOSE 8080

# Run the JAR file as the entrypoint
ENTRYPOINT [ "/jre/bin/java", "-jar", "/app/app.jar" ]
