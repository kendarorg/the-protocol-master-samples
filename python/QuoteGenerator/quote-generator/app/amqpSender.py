import logging
from datetime import datetime
from random import uniform
from threading import Thread
from time import sleep

import jsons
from autowired import component

from app.quotationMessage import QuotationMessage
from app.quotationStatus import QuotationStatus
from rabbit.lib.rabbitMq import RabbitMQ
from utils.appSettings import AppSettings
from utils.autostart import Autostart


class AmqpSenderThread(Thread):
    def __init__(self, thread_name, thread_id):
        self.log = logging.getLogger("RabbitMQ")
        self.log.info("START AmqpSenderThread")
        Thread.__init__(self)
        self.app_settings = None
        self.rabbit_mq = None
        self.thread_name = thread_name
        self.thread_ID = thread_id

    def setup(self, rabbit_mq: RabbitMQ, app_settings: AppSettings):
        self.rabbit_mq = rabbit_mq
        self.app_settings = app_settings

    def run(self):
        self.log.info("RUN AmqpSenderThread")
        volatility = 0.2
        quotations = [QuotationStatus(symbol="META",
                                      price=self.random_value(0, 100),
                                      volume=int(self.random_value(10, 1000)))]
        while True:
            self.log.info("SENDING")
            try:
                for quotation in quotations:
                    old_price = quotation.price
                    rnd = self.random_value(0, 100) / 100;
                    change_percent = 2 * volatility * rnd
                    if change_percent > volatility:
                        change_percent = change_percent - (2 * volatility)

                    change_amount = quotation.price * change_percent
                    quotation.price = (old_price + change_amount)
                    quotation.volume = (int(self.random_value(1, 10000)))
                    quotation_message = QuotationMessage(symbol=quotation.symbol,
                                                         price=round(quotation.price, 3),
                                                         volume=quotation.volume,
                                                         date=datetime.now())
                    message_string = jsons.dumps(quotation_message)
                    self.rabbit_mq.publish(queue_name=self.app_settings.rabbit_queue,
                                           message=message_string,
                                           exchange=self.app_settings.rabbit_exchange)
                self.log.info("SENT")
            except Exception as e:
                self.log.error("Unable to send data to RabbitMq {0}".format(e))

            sleep(10)

    @staticmethod
    def random_value(minimum, maximum):
        if abs(minimum - maximum) < 2:
            maximum = minimum + 2
        return uniform(abs(minimum - maximum), abs(minimum))


@component
class AmqpSender(Autostart):
    def __init__(self, rabbit_mq: RabbitMQ, app_settings: AppSettings):
        sender = AmqpSenderThread("sender", 1000)
        sender.setup(rabbit_mq, app_settings)
        sender.start()
