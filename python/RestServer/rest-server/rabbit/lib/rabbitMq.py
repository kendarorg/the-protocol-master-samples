from threading import Thread
from time import sleep
import logging

from autowired import component
from kombu import Connection, Exchange, Queue, Consumer, Producer

from rabbit.lib.rabbitConsumer import RabbitConsumer
from rabbit.lib.worker import Worker
from utils.appSettings import AppSettings
from utils.autostart import Autostart


@component
class RabbitMQ(Autostart):
    rabbit_url = ""

    def __init__(self, app_settings: AppSettings, consumers: list[RabbitConsumer]):
        self.log = logging.getLogger("RabbitMQ")
        try:
            self.log.info("Initializing")
            self.queue = app_settings.rabbit_queue
            self.user = app_settings.rabbit_user
            self.password = app_settings.rabbit_password
            self.host = app_settings.rabbit_host
            self.port = app_settings.rabbit_port
            self.exchange = app_settings.rabbit_exchange
            self.rabbit_url = "amqp://" + self.host + ":" + str(self.port) + "/";
            self.channel = None
            self.consumers = consumers
            self.connect()
            self.connection = None
            for consumer in self.consumers:
                if consumer.callback is not None:
                    Thread(target=(lambda: self.consume(consumer.queue, consumer.callback, self.exchange))).start()

        except Exception as e:
            self.log.error("Unable to initialize RabbitMq {0}".format(e))

    def connect(self):
        None

    def close(self):
        if self.connection and not self.connection.is_closed:
            self.connection.close()

    def consume(self, queue_name, callback, exchange_name=''):
        self.log.info("Initializing queue "+queue_name)
        with Connection(self.rabbit_url,
                        userid=self.user,
                        password=self.password,
                        heartbeat=20) as conn:

            # conn_retry_opts = {
            #     'max_retries': 30,  # Retry forever
            #     'interval_start': 1,  # First retry after 1 second
            #     'interval_step': 1,  # Increase interval by 1 second each retry
            #     'interval_max': 30,  # Maximum wait is 30 seconds
            # }

            # Ensure connection with retry
            # conn.ensure_connection(**conn_retry_opts)
            worker = Worker(conn, queue_name, exchange_name, callback)
            worker.run()

    def publish(self, queue_name, message, exchange=''):
        self.log.info("Publish on "+queue_name)
        try:
            if self.connection is None:
                self.connection = Connection(self.rabbit_url, userid=self.user, password=self.password, heartbeat=20)
            channel = self.connection.channel()
            producer = Producer(channel)
            producer.publish(
                message,
                routing_key=queue_name,
                retry=True,
                exchange=exchange,
                content_type="application/json"
            )
            self.log.info(f"Sent message to queue {queue_name}: {message}")

        except Exception as e:
            sleep(5)
            self.log.info("Unable to initialize Rabbit {0}".format(e))
            self.connection = None

