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
    method_names = dir(clazz)
    result = {}
    for method_name in method_names:
        if method_name.startswith("_"):
            continue
        method = getattr(clazz, method_name)
        if not hasattr(method, "_decorators"):
            continue
        result[method_name + str(signature(method))] = {'decorators': method._decorators,
                                                        'method': method,
                                                        'name': method_name}
    return result
