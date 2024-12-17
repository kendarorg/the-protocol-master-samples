class RabbitConsumer:

    def __init__(self, queue: str, callback):
        self.queue = queue
        self.callback = callback
