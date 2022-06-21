#
# Build stage
#
FROM maven:3.8.1-jdk-11-openj9 AS build
WORKDIR /app
COPY src /app/src
COPY pom.xml /app
RUN mvn clean compile assembly:single
RUN ls -al target/

#
# Package stage
#
FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=build /app/target/CargoBot-1.0-SNAPSHOT-jar-with-dependencies.jar /app/CargoBot.jar
ENTRYPOINT ["java","-jar","/app/CargoBot.jar"]
