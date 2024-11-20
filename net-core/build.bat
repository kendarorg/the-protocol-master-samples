
docker build . -t net_core_tpm . -f Dockerfile.tpm
docker build . -t net_core_http . -f Dockerfile.HttpServer
docker build . -t net_core_rest . -f Dockerfile.RestServer