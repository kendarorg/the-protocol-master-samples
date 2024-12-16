FROM python:3.9.13

WORKDIR /app

RUN pip install autowired bottle configparser mysql-connector-python mysql pymysql pewee
ADD chat-server /app
COPY application.properties /app/properties.ini


CMD ["python", "main.py"]

HEALTHCHECK --interval=10s --retries=30 CMD curl --fail http://localhost:80/api/status || exit 1