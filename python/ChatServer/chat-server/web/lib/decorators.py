import functools
from inspect import signature


def qroute(path, verb="GET"):
    def decorator(func):
        if not hasattr(func, "_decorators"):
            func._decorators = []
        func._decorators.append({'type': 'qroute', 'path': path, 'verb': verb})

        @functools.wraps(func)
        def wrapper(*a, **ka):
            return func(*a, **ka)

        return wrapper

    return decorator


def qauth(provider, permissions: [str] = []):
    def decorator(func):
        if not hasattr(func, "_decorators"):
            func._decorators = []
        func._decorators.append({'type': 'qauth', 'provider': provider, 'permissions': permissions})

        @functools.wraps(func)
        def wrapper(*a, **ka):
            return func(*a, **ka)

        return wrapper

    return decorator


def find_decorators(clazz):
    methodNames = dir(clazz)
    result = {}
    for methodName in methodNames:
        if methodName.startswith("_"):
            continue
        method = getattr(clazz, methodName)
        if not hasattr(method, "_decorators"):
            continue
        result[methodName + str(signature(method))] = {'decorators': method._decorators, 'method': method,
                                                       'name': methodName}
    return result