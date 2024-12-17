from datetime import time, datetime
from random import uniform
from time import sleep

from autowired import component

from app.quotationMessage import QuotationMessage
from app.quotationStatus import QuotationStatus
from rabbit.lib.rabbitMq import RabbitMQ
from utils.appSettings import AppSettings
from utils.autostart import Autostart
from utils.jsonMapper import to_json_with_iso_dates


def random_value(param, param1):
    pass


@component
class AmqpSender(Autostart):
    def __init__(self, rabbit_mq: RabbitMQ, app_settings: AppSettings):
        volatility = 0.2
        quotations = [QuotationStatus(symbol="META",
                                      price=self.random_value(0, 100),
                                      volume=int(self.random_value(10, 1000)))]
        while True:
            for quotation in quotations:
                old_price = quotation.price
                rnd = self.random_value(0, 100) / 100;
                change_percent = 2 * volatility * rnd
                if change_percent > volatility:
                    change_percent = change_percent - (2 * volatility)

                change_amount = quotation.price * change_percent
                quotation.price = (old_price + change_amount)
                quotation.volume = (int(random_value(1, 10000)))
                quotation_message = QuotationMessage(symbol=quotation.symbol,
                                                     price=round(quotation.price, 3),
                                                     volume=quotation.volume)
                quotation_message.date = datetime.now()
                message_string = to_json_with_iso_dates(quotation_message)
                rabbit_mq.publish(queue_name=app_settings.rabbit_queue,
                                  message=message_string,
                                  exchange=app_settings.rabbit_exchange)
            sleep(10)

    @staticmethod
    def random_value(minimum, maximum):
        if abs(minimum - maximum) < 2:
            maximum = minimum + 2
        return uniform(abs(minimum - maximum), abs(minimum))
