FROM adoptopenjdk/openjdk11:jdk-11.0.11_9-alpine-slim AS build

WORKDIR /app
RUN apk add --no-cache maven
ADD quote-generator /app
RUN mvn clean package -DskipTests

FROM adoptopenjdk/openjdk11:jdk-11.0.11_9-alpine-slim
RUN apk add --no-cache curl

WORKDIR /app

COPY --from=build /app/target/quote-generator-1.0.0.jar .
COPY application.properties /app/application.properties

EXPOSE 80

CMD ["java", "-jar", "quote-generator-1.0.0.jar"]

HEALTHCHECK --interval=3s CMD curl --fail http://localhost:80/api/status || exit 1