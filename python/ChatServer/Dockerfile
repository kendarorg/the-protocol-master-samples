FROM python:3.9.13

WORKDIR /app

RUN pip install autowired bottle
ADD chat-server /app


CMD ["python", "main.py"]

HEALTHCHECK --interval=10s --retries=30 CMD curl --fail http://localhost:80/api/status || exit 1