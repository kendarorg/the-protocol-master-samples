import os
import pkgutil
import sys
from importlib import import_module
from typing import Type, TypeVar

from autowired import Context

from utils.appSettings import AppSettings

_T = TypeVar("_T")
Module = type(sys)


class ApplicationContext(Context):

    def load_modules_in_dir(self, package_dir):
        packageDirName = package_dir.replace("/", ".").replace("...", "")
        for (_, module_name, is_pkg) in pkgutil.iter_modules([package_dir]):
            # import the module and iterate through its attributes
            try:
                module = import_module(f"{packageDirName}.{module_name}")
                self.container.component_scan(module)
            except:
                continue
            finally:
                continue

    def load_submodules(self, package_dir):
        for subdir, dirs, files in os.walk( package_dir):
            if not subdir.endswith("_"):
                self.load_modules_in_dir(subdir)

    def __init__(self, *args: Module):
        for val in args:
            self.load_submodules(str(os.path.basename(val.__path__[0])))

        self.singletons = {}

    def resolve(
            self,
            t: Type[_T]
    ) -> _T:
        return self.container.resolve(t)