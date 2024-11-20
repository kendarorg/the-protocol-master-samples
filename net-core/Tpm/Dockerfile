FROM adoptopenjdk/openjdk11:jdk-11.0.11_9-alpine-slim
RUN apk add --no-cache bash wget

COPY  settings.json /settings.json
COPY  rewrite.json /rewrite.json
#COPY  protocol-runner.jar /protocol-runner.jar
COPY  ./start.sh /tpmrunner.sh
RUN chmod +x /tpmrunner.sh
#WORKDIR /
ENTRYPOINT ["/tpmrunner.sh"]