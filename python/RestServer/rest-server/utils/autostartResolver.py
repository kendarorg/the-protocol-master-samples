from autowired import component

from utils.autostart import Autostart

@component
class AutostartResolver:
    def __init__(self,auto_starts: list[Autostart] ):
        self.auto_starts = auto_starts