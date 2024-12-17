FROM python:3.9.13

WORKDIR /app

RUN pip install autowired bottle configparser mysql-connector-python mysql pymysql peewee pika
ADD chat-server /app
COPY properties.ini /app/properties.ini


CMD ["python", "main.py"]

HEALTHCHECK --interval=10s --retries=30 CMD curl --fail http://localhost:80/api/status || exit 1