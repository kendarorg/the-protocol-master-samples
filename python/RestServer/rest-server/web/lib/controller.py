from abc import ABC

from autowired import component


class Controller(ABC):
    def map_routes(self, controller_mapper):
        pass


@component
class EmptyController(Controller):
    pass