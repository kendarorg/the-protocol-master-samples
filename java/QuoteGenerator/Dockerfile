FROM adoptopenjdk/openjdk11:jdk-11.0.11_9-alpine-slim AS build
WORKDIR /app
COPY quote-generator/pom.xml .
COPY quote-generator/src ./src
RUN mvn clean package -DskipTests

FROM adoptopenjdk/openjdk11:jdk-11.0.11_9-alpine-slim
RUN apk add --no-cache bash wget

WORKDIR /app

COPY --from=build /app/target/quote-generator-1.0.0.jar .
COPY application.properties /app/application.properties

CMD ["java", "-jar", "quote-generator-1.0.0.jar"]