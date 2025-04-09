import logging

import jsons
from kombu import Queue, Consumer
from kombu.mixins import ConsumerMixin


class Worker(ConsumerMixin):
    def __init__(self, connection, queue_name, exchange_name, callback):
        self.log = logging.getLogger("Worker")
        self.connection = connection
        self.queue_name = queue_name
        self.exchange_name = exchange_name
        self.callback = callback

    def on_connection_error(self, exc, interval):
        self.log.error("Connection closed")
        self.should_stop = True

    # def consume(self, *args, **kwargs):
    #    consume = self.connection.ensure(self.connection, super().consume)
    #    return consume(*args, **kwargs)

    def get_consumers(self, consumer, channel):
        return [Consumer(queues=[Queue(self.queue_name,
                                       exchange=self.exchange_name,
                                       routing_key=self.queue_name)],
                         channel=channel,
                         callbacks=[self.on_message],
                         accept=["application/json"])]

    def on_message(self, body, message):
        self.log.info("MESSAGE RECV")
        self.callback(message.channel, message.delivery_tag, message.properties, jsons.dumps(body))
        message.ack()
