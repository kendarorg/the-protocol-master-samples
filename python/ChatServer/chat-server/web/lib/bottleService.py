import bottle
from autowired import component

from utils.applicationContext import ApplicationContext
from utils.appSettings import AppSettings
from web.lib.authProvider import AuthProvider
from web.lib.bottleBuilder import BottleBuilder
from web.lib.controller import Controller
from web.lib.decorators import find_decorators
from web.lib.nullAuthProvider import NullAuthProvider


@component
class BottleService:
    def __init__(self, controllers: list[Controller], application_context: ApplicationContext, app_settings: AppSettings):

        auth_provider = application_context.try_resolve(AuthProvider)
        if auth_provider is None:
            auth_provider = NullAuthProvider()
        self.controllers = controllers
        self.app_settings = app_settings
        for controller in self.controllers:
            controller.map_routes(self)
            t = type(controller)
            decorators = find_decorators(t)
            for key, value in decorators.items():
                instance_method = getattr(controller, value["name"])
                decorators = value["decorators"]
                route = [p for p in decorators if p["type"] == "qroute"]
                auth = [p for p in decorators if p["type"] == "qauth"]
                if len(route) == 1:
                    route = route[0]
                    if len(auth) == 1:
                        auth = auth[0]
                        if auth["permissions"] is not None and len(auth["permissions"]) > 0:
                            permission_decorator = AuthProvider.permission(auth_provider.check_permission,
                                                                           auth["permissions"])
                            instance_method = permission_decorator(instance_method)
                        auth_decorator = AuthProvider.auth_basic(auth_provider.check_auth)
                        instance_method = auth_decorator(instance_method)
                    bottle.route(route["path"], callback=instance_method, method=route["verb"])

    @staticmethod
    def get(path, callback):
        return BottleBuilder(path, callback, "GET")

    @staticmethod
    def post(path, callback):
        return BottleBuilder(path, callback, "POST")

    @staticmethod
    def put(self, path, callback):
        return BottleBuilder(path, callback, "PUT")

    @staticmethod
    def delete(self, path, callback):
        return BottleBuilder(path, callback, "DELETE")

    def run(self, app=None, server='wsgiref',
            interval=1, reloader=False, quiet=False, plugins=None,
            **kargs):
        bottle.run(app, server, self.app_settings.host, self.app_settings.port, interval, reloader, quiet, plugins,
                   self.app_settings.debug, **kargs)
