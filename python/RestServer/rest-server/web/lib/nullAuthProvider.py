from web.lib.authProvider import AuthProvider


class NullAuthProvider(AuthProvider):
    def check_auth(self, login, password):
        return True

    def check_permission(self, login, permissions: [str]):
        return True
