from autowired import component


class RabbitConsumer:

    def __init__(self, queue: str, callback):
        self.queue = queue
        self.callback = callback


@component
class EmptyConsumer(RabbitConsumer):
    def __init__(self):
        RabbitConsumer.__init__(self, "", None)
