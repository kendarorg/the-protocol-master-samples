import os
import sys
from importlib import import_module
from typing import Type, TypeVar

from autowired import Context

from utils.autostart import Autostart
from utils.autostartResolver import AutostartResolver

_T = TypeVar("_T")
Module = type(sys)


class ApplicationContext(Context):

    def __init__(self, *args: Module):
        self.container.add(self)
        for val in args:
            package_dir_name = str(os.path.basename(val.__path__[0]))
            module = import_module(f"{package_dir_name}")
            print("Loaded module " + package_dir_name)
            self.container.component_scan(module)

        autorunnable = self.try_resolve(AutostartResolver)
        self.singletons = {}

    def try_resolve(
            self,
            t: Type[_T]
    ) -> _T:
        try:
            return self.container.resolve(t)
        except Exception as ex:
            print(ex)
            return None

    def resolve(
            self,
            t: Type[_T]
    ) -> _T:
        return self.container.resolve(t)
