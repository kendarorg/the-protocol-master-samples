import functools
from abc import ABC, abstractmethod

from bottle import request, HTTPError


class AuthProvider(ABC):
    @abstractmethod
    def check_auth(self, login, password):
        pass

    @abstractmethod
    def check_permission(self, login, permissions: [str]):
        pass

    @staticmethod
    def auth_basic(check, realm="private", text="Access denied"):
        """ Callback decorator to require HTTP auth (basic).
            TODO: Add route(check_auth=...) parameter. """

        def decorator(func):
            @functools.wraps(func)
            def wrapper(*a, **ka):
                user, password = request.auth or (None, None)
                if user is None or not check(user, password):
                    err = HTTPError(401, text)
                    err.add_header('WWW-Authenticate', 'Basic realm="%s"' % realm)
                    return err
                return func(*a, **ka)

            return wrapper

        return decorator

    @staticmethod
    def permission(check, permissions, realm="private", text="Access denied"):
        """ Callback decorator to require HTTP auth (basic).
            TODO: Add route(check_auth=...) parameter. """

        def decorator(func):
            @functools.wraps(func)
            def wrapper(*a, **ka):
                user, password = request.auth or (None, None)
                if user is None or not check(user, permissions):
                    err = HTTPError(403, text)
                    return err
                return func(*a, **ka)

            return wrapper

        return decorator
