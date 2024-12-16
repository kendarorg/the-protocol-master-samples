from autowired import component

from web.lib.controller import Controller
from web.lib.decorators import qroute


@component
class HelloController(Controller):
    @qroute("/api/status")
    def hello(self):
        return ("OK")