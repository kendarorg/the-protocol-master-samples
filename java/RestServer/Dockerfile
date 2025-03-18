FROM adoptopenjdk/openjdk11:jdk-11.0.11_9-alpine-slim AS build

WORKDIR /app
RUN apk add --no-cache maven
ADD rest-server /app


RUN mvn clean install -DskipTests

FROM adoptopenjdk/openjdk11:jdk-11.0.11_9-alpine-slim
RUN apk add --no-cache curl bash

WORKDIR /app

COPY --from=build /app/target/rest-server-1.0.0.jar .
COPY application.properties /app/application.properties

EXPOSE 80

CMD ["java", "-jar", "rest-server-1.0.0.jar","-Dspring.config.location=application.properties"]

HEALTHCHECK --interval=10s --retries=30 CMD curl --fail http://localhost:80/api/status || exit 1