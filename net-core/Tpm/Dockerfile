FROM amazoncorretto:17.0.7-alpine
RUN apk add --no-cache bash wget curl dos2unix

COPY  settings.json /settings.json
COPY  rewrite.json /rewrite.json
COPY  protocol-runner.jar /protocol-runner.jar
COPY  ./start.sh /tpmrunner.sh
RUN dos2unix /tpmrunner.sh
RUN chmod +x /tpmrunner.sh
ENTRYPOINT ["/tpmrunner.sh"]

EXPOSE 5005
EXPOSE 3306
EXPOSE 8081
EXPOSE 9000
EXPOSE 80

HEALTHCHECK --interval=3s --retries=30  CMD curl --fail http://localhost:8081/api/status || exit 1