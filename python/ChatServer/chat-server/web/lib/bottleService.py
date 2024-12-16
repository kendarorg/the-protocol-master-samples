import bottle
from autowired import component

from web.lib.authProvider import AuthProvider
from web.lib.bottleBuilder import BottleBuilder
from web.lib.controller import Controller
from web.lib.decorators import find_decorators


@component
class BottleService:
    def __init__(self, controllers: list[Controller], authProvider: AuthProvider):
        self.controllers = controllers
        for controller in self.controllers:
            controller.mapRoutes(self)
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
                            permissionDecorator = AuthProvider.permission(authProvider.checkPermission,
                                                                          auth["permissions"])
                            instance_method = permissionDecorator(instance_method)
                        authDecorator = AuthProvider.auth_basic(authProvider.checkAuth)
                        instance_method = authDecorator(instance_method)
                    bottle.route(route["path"], callback=instance_method, method=route["verb"])

    def get(self, path, callback):
        return BottleBuilder(path, callback, "GET")

    def post(self, path, callback):
        return BottleBuilder(path, callback, "POST")

    def put(self, path, callback):
        return BottleBuilder(path, callback, "PUT")

    def delete(self, path, callback):
        return BottleBuilder(path, callback, "DELETE")

    def run(self, app=None, server='wsgiref', host='127.0.0.1', port=8080,
            interval=1, reloader=False, quiet=False, plugins=None,
            debug=None, **kargs):
        bottle.run(app, server, host, port, interval, reloader, quiet, plugins, debug, **kargs)