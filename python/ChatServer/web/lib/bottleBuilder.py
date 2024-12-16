import bottle

from web.lib.authProvider import AuthProvider


class BottleBuilder:
    def __init__(self, path, callback, verb):
        self.verb = verb
        self.callback = callback
        self.path = path
        self.auth_provider = None
        self.permissions = None

    def withAuth(self, auth_provider: AuthProvider):
        self.auth_provider = auth_provider
        return self

    def withPermissions(self, permissions: list[str]):
        self.permissions = permissions
        return self

    def build(self):
        cb = self.callback
        if self.auth_provider is not None:
            if self.permissions is not None:
                permissionDecorator = AuthProvider.permission(self.auth_provider.checkPermission, self.permissions)
                cb = permissionDecorator(cb)
            decorator = AuthProvider.auth_basic(self.auth_provider.checkAuth)
            cb = decorator(cb)
        bottle.route(self.path, callback=cb, method=self.verb)