FROM amazoncorretto:17.0.7-alpine
RUN apk add --no-cache bash wget curl dos2unix
RUN mkdir /plugins
COPY  settings.json /settings.json
COPY  protocol-runner.jar /protocol-runner.jar
COPY  protocol-dns-plugin-3.0.2.jar /plugins/protocol-dns-plugin-3.0.2.jar
COPY  ./start.sh /tpmrunner.sh
RUN dos2unix /tpmrunner.sh
RUN chmod +x /tpmrunner.sh
ENTRYPOINT ["/tpmrunner.sh"]

HEALTHCHECK --interval=3s --retries=30  CMD curl --fail http://localhost:8081/api/status || exit 1