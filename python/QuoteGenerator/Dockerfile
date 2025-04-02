FROM python:3.9.13

WORKDIR /app

#RUN pip install autowired kombu bottle configparser pika cryptography jsons
ADD quote-generator /app
COPY properties.ini /app/properties.ini
RUN pip install -r requirements.txt


EXPOSE 80

CMD ["python", "main.py"]

HEALTHCHECK --interval=10s --retries=30 CMD curl --fail http://localhost:80/api/status || exit 1