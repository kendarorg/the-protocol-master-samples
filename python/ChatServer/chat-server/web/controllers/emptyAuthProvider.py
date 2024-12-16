from autowired import component

from web.lib.nullAuthProvider import NullAuthProvider


@component
class EmptyAuthProvider(NullAuthProvider):
    pass
