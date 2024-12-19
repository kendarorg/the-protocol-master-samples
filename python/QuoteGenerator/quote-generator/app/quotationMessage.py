from datetime import datetime


class QuotationMessage:

    def __init__(self, symbol: str, price: float, volume: int, date: datetime):
        self.symbol = symbol
        self.price = price
        self.volume = volume
        self.date = date
