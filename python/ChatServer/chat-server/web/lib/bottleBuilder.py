import bottle

from web.lib.authProvider import AuthProvider


class BottleBuilder:
    def __init__(self, path, callback, verb):
        self.verb = verb
        self.callback = callback
        self.path = path
        self.auth_provider = None
        self.permissions = None

    def with_auth(self, auth_provider: AuthProvider):
        self.auth_provider = auth_provider
        return self

    def with_permissions(self, permissions: list[str]):
        self.permissions = permissions
        return self

    def build(self):
        cb = self.callback
        if self.auth_provider is not None:
            if self.permissions is not None:
                permission_decorator = AuthProvider.permission(self.auth_provider.check_permission, self.permissions)
                cb = permission_decorator(cb)
            decorator = AuthProvider.auth_basic(self.auth_provider.check_auth)
            cb = decorator(cb)
        bottle.route(self.path, callback=cb, method=self.verb)
