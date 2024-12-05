
docker build . -t net-core-tpm . -f Dockerfile.tpm
docker build . -t net-core-http . -f Dockerfile.HttpServer
docker build . -t net-core-rest . -f Dockerfile.RestServer