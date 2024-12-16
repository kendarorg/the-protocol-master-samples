import os
from pathlib import Path

from autowired import component
from bottle import static_file

from web.lib.controller import Controller


def staticControllerCallbackFactory(fileName,root):
    return lambda: staticControllerRender(fileName,root)


def staticControllerRender(fileName,root):
    return static_file(fileName, root)


@component
class StaticController(Controller):

    def mapRoutes(self, controllerMapper):
        staticPathString = str(Path(__file__).resolve().parent.parent) + os.sep+"static"

        for currentpath, folders, files in os.walk(staticPathString):
            for file in files:
                content = Path(os.path.join(currentpath, file))
                osDependantSubPath = str(content).replace(staticPathString, "")
                subPath = osDependantSubPath.replace(os.sep,'/')
                controllerMapper.get(subPath, staticControllerCallbackFactory(osDependantSubPath,staticPathString)).build()
                if subPath.endswith("index.html") or subPath.endswith("index.htm"):
                    subPath = subPath.replace("index.html","")
                    subPath = subPath.replace("index.htm","")
                    controllerMapper.get(subPath, staticControllerCallbackFactory(osDependantSubPath,staticPathString)).build()