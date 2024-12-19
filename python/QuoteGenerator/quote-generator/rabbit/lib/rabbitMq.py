import pika
from autowired import component

from rabbit.lib.rabbitConsumer import RabbitConsumer
from utils.appSettings import AppSettings
from utils.autostart import Autostart


@component
class RabbitMQ(Autostart):
    def __init__(self, app_settings: AppSettings, consumers: list[RabbitConsumer]):
        try:
            self.queue = app_settings.rabbit_queue
            self.user = app_settings.rabbit_user
            self.password = app_settings.rabbit_password
            self.host = app_settings.rabbit_host
            self.port = app_settings.rabbit_port
            self.connection = None
            self.channel = None
            self.connect()
            for consumer in consumers:
                if consumer.callback is not None:
                    self.consume(consumer.queue, consumer.callback, app_settings.rabbit_exchange)
        except Exception as e:
            print("Unable to initialize RabbitMq ", e)

    def connect(self):
        credentials = pika.PlainCredentials(self.user, self.password)
        parameters = pika.ConnectionParameters(host=self.host, port=self.port, credentials=credentials)
        self.connection = pika.BlockingConnection(parameters)
        self.channel = self.connection.channel()

    def close(self):
        if self.connection and not self.connection.is_closed:
            self.connection.close()

    def consume(self, queue_name, callback, exchange=''):
        if not self.channel:
            raise Exception("Connection is not established.")
        self.channel.exchange_declare(exchange=exchange)
        self.channel.queue_declare(queue=queue_name, durable=True)
        self.channel.queue_bind(queue=queue_name, exchange=exchange, routing_key=queue_name)
        self.channel.basic_consume(queue=queue_name, on_message_callback=callback, auto_ack=True)
        self.channel.start_consuming()

    def send_heartbeat(self):
        self.connection.process_data_events()

    def publish(self, queue_name, message, exchange=''):
        if not self.channel:
            raise Exception("Connection is not established.")
        self.channel.queue_declare(queue=queue_name, durable=True)
        self.channel.exchange_declare(exchange=exchange)
        self.channel.queue_bind(queue=queue_name, exchange=exchange)
        self.channel.basic_publish(exchange=exchange,
                                   routing_key=queue_name,
                                   body=message,
                                   properties=pika.BasicProperties(
                                       delivery_mode=2,  # make message persistent
                                   ))
        print(f"Sent message to queue {queue_name}: {message}")
