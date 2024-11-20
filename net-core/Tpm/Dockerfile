FROM adoptopenjdk/openjdk11:jdk-11.0.11_9-alpine-slim
RUN apk add --no-cache bash wget


RUN wget https://github.com/kendarorg/the-protocol-master/releases/download/v2.0.0/protocol-runner.jar

COPY  settings.ini /settings.ini
COPY  rewrite.json /rewrite.json

ENTRYPOINT ["java", "-jar","/protocol-runner.jar", "-cfg","/settings.ini"]