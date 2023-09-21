#
# Build stage
#
FROM maven:3.8.7-eclipse-temurin-19 AS MAVEN_BUILD
COPY pom.xml /build/
COPY . /build/
WORKDIR /build/
#The option "--quiet" is used to limit the output to only warnings and errors (1)
RUN mvn -f /build/pom.xml clean package --quiet

#
# Package stage
#
# base image to build a JRE
FROM amazoncorretto:19.0.2-alpine AS deps

# Identify dependencies (2)
COPY --from=MAVEN_BUILD ./build/target/*-SNAPSHOT.jar /app/app.jar
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


## run this command with this option to display dependencies
##  docker build -t discover-jdeps  .  --progress plain -f ./jdeps/Dockerfile
RUN jdeps_file="$(cat /deps.info)" && echo $jdeps_file
