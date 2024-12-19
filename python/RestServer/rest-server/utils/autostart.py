from autowired import component


class Autostart:
    pass


@component
class EmptyAutostart(Autostart):
    pass
