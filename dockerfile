#
# Build stage
#
FROM maven:3.6.0-jdk-11-slim AS MAVEN_BUILD
COPY pom.xml /build/
COPY src /build/src/
WORKDIR /build/
RUN mvn -f /build/pom.xml clean package


#
# Package stage
#
FROM adoptopenjdk/openjdk11:x86_64-alpine-jdk-11.0.1.13-slim
VOLUME /tmp
COPY  --from=MAVEN_BUILD /build/target/spring-boot-efficient-search-api-0.0.1-SNAPSHOT.jar  spring-boot-efficient-search-api.jar
ENTRYPOINT ["java","-jar","spring-boot-efficient-search-api.jar"]
