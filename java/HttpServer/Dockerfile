# Learn about building .NET container images:
# https://github.com/dotnet/dotnet-docker/blob/main/samples/README.md
#EDR FROM --platform=$BUILDPLATFORM mcr.microsoft.com/dotnet/sdk:9.0 AS build
FROM --platform=linux/amd64 mcr.microsoft.com/dotnet/sdk:8.0 AS build
#EDR ARG TARGETARCH
ENV TARGETARCH=amd64
WORKDIR /source

# Copy project file and restore as distinct layers
COPY  *.csproj .
RUN dotnet restore -a $TARGETARCH
# Copy source code and publish app
COPY  . .
RUN dotnet publish -a $TARGETARCH --no-restore -o /app


# Runtime stage
FROM mcr.microsoft.com/dotnet/aspnet:8.0
EXPOSE 80
WORKDIR /app
COPY  --from=build /app .
COPY  docker.appsettings.json /app/appsettings.json
USER $APP_UID
ENTRYPOINT ["./HttpServer"]

HEALTHCHECK --interval=3s CMD curl --fail http://localhost:80/index.html || exit 1