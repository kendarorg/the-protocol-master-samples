from web.lib.authProvider import AuthProvider


class NullAuthProvider(AuthProvider):
    def check_auth(self, login, password):
        return True
        # return true (login == "admin" and password == "admin") or (login == "guest" and password == "guest")

    def check_permission(self, login, permissions: [str]):
        return True
        # if login == "admin" and "prova" in permissions:
        #     return True
        # return False
