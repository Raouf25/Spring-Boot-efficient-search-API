#
# Build stage
#
FROM maven:3.8.7-eclipse-temurin-19 AS maven_build
COPY pom.xml /build/
COPY . /build/
WORKDIR /build/
#The option "--quiet" is used to limit the output to only warnings and errors (1)
RUN mvn clean package -DskipTests --quiet

#
# Package stage
#
# base image to build a JRE
FROM amazoncorretto:19.0.2-alpine AS deps

# Identify dependencies (2)
COPY --from=maven_build ./build/target/*-SNAPSHOT.jar /app/app.jar
RUN mkdir /app/unpacked && \
    cd /app/unpacked && \
    unzip ../app.jar && \
    cd .. && \
    $JAVA_HOME/bin/jdeps \
    --ignore-missing-deps \
    --print-module-deps \
    -q \
    --recursive \
    --multi-release 17 \
    --class-path="./unpacked/BOOT-INF/lib/*" \
    --module-path="./unpacked/BOOT-INF/lib/*" \
    ./app.jar > /deps.info && \
    rm -rf ./unpacked

# base image to build a JRE
FROM amazoncorretto:19.0.2-alpine AS corretto-jdk

# required for strip-debug to work
RUN apk add --no-cache binutils

# copy module dependencies info
COPY --from=deps /deps.info /deps.info

# Build small JRE image (3)
RUN $JAVA_HOME/bin/jlink \
         --verbose \
         --add-modules $(cat /deps.info) \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /customjre

## run this command with this option to display dependencies
##  docker build -t spring-boot-efficient-search-api-custom-5  .  --progress plain
RUN file0="$(cat /deps.info)" && echo $file0

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
COPY --chown=1000:1000  --from=maven_build /build/target/spring-boot-efficient-search-api-0.0.1-SNAPSHOT.jar  /app/app.jar

# Expose port 8080
EXPOSE 8080

# Run the JAR file as the entrypoint
ENTRYPOINT [ "/jre/bin/java", "-jar", "/app/app.jar" ]

# docker build -t spring-boot-efficient-search-api-1  .
# docker run -t -p 8080:8080  spring-boot-efficient-search-api-1
# docker images --filter=reference='spring-boot-efficient-search-api*'