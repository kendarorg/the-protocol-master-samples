FROM golang:1.18-bullseye

RUN go install github.com/beego/bee/v2@latest

ENV GO111MODULE=on
ENV GOFLAGS="-mod=vendor"
ENV APP_HOME=/app

WORKDIR /app
ADD go-rest /app
RUN go mod vendor
COPY properties.ini /app/properties.ini

EXPOSE 80
CMD ["bee", "run"]

HEALTHCHECK --interval=10s --retries=30 CMD curl --fail http://localhost:80/api/status || exit 1