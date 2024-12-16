FROM python:3.9.13

WORKDIR /app

RUN pip install autowired bottle
ADD chat-server /app


CMD ["python", "main.py"]