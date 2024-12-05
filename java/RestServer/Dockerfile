FROM adoptopenjdk/openjdk11:jdk-11.0.11_9-alpine-slim AS build
WORKDIR /app
COPY rest-server/pom.xml .
COPY rest-server/src ./src
RUN mvn clean package -DskipTests

FROM adoptopenjdk/openjdk11:jdk-11.0.11_9-alpine-slim
RUN apk add --no-cache bash wget

WORKDIR /app

COPY --from=build /app/target/rest-server-1.0.0.jar .
COPY application.properties /app/application.properties

CMD ["java", "-jar", "rest-server-1.0.0.jar"]

HEALTHCHECK --interval=3s CMD curl --fail http://localhost:80/index.html || exit 1