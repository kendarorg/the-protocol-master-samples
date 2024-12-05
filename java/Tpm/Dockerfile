FROM adoptopenjdk/openjdk11:jdk-11.0.11_9-alpine-slim
RUN apk add --no-cache bash wget

COPY  settings.json /settings.json
COPY  rewrite.json /rewrite.json
COPY  ./start.sh /tpmrunner.sh
RUN chmod +x /tpmrunner.sh
ENTRYPOINT ["/tpmrunner.sh"]