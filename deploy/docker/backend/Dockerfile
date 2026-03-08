FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd
COPY src src

RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN apk add --no-cache \
    nodejs npm \
    chromium chromium-chromedriver \
    nss freetype harfbuzz ttf-freefont \
    ca-certificates

COPY --from=build /workspace/target/auto-web-generator-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8123

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
