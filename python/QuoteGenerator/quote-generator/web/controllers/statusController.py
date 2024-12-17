from autowired import component

from web.lib.controller import Controller
from web.lib.decorators import qroute


@component
class StatusController(Controller):
    @qroute("/api/status")
    def hello(self):
        return "OK"
