import json

from autowired import component

from db.entitities.quotation import Quotation
from rabbit.lib.rabbitConsumer import RabbitConsumer
from utils.appSettings import AppSettings


@component
class RabbitMessagesHandler(RabbitConsumer):
    def __init__(self,app_settings:AppSettings):
        super().__init__(app_settings.rabbit_queue, self.callback)

    def callback(self, method, properties, body):
        data = json.loads(body)
        quotation = Quotation()
        quotation.date = data["date"]
        quotation.price = data["price"]
        quotation.symbol = data["symbol"]
        quotation.volume = data["volume"]
        quotation.save()
        print(f"Received message: {body}")