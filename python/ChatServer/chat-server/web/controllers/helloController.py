from autowired import component

from web.lib.authProvider import AuthProvider
from web.lib.controller import Controller
from web.lib.decorators import qroute, qauth


@component
class HelloController(Controller):
    @qroute("/api/hello")
    def hello(self):
        return "Hello"

    @qroute("/api/hello/<name>")
    def hello_name(self, name):
        return "Hello " + name

    @qroute("/api/auth/<name>")
    @qauth(AuthProvider)
    def hello_auth(self, name):
        return "Hello " + name

    @qroute("/api/auth/prova")
    @qauth(AuthProvider, ["prova"])
    def hello_auth_prova_ok(self):
        return "Hello Prova"

    @qroute("/api/auth/fault")
    @qauth(AuthProvider, ["fault"])
    def hello_auth_prova_ko(self):
        return "Hello Fault"
