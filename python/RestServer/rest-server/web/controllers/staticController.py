import os
from pathlib import Path

from autowired import component
from bottle import static_file

from web.lib.controller import Controller


def static_controller_callback_factory(file_name, root):
    return lambda: static_controller_render(file_name, root)


def static_controller_render(file_name, root):
    return static_file(file_name, root)


@component
class StaticController(Controller):

    def map_routes(self, controller_mapper):
        static_path_string = str(Path(__file__).resolve().parent.parent.parent) + os.sep + "static"

        for currentpath, folders, files in os.walk(static_path_string):
            for file in files:
                content = Path(os.path.join(currentpath, file))
                os_dependant_sub_path = str(content).replace(static_path_string, "")
                sub_path = os_dependant_sub_path.replace(os.sep, '/')
                controller_mapper.get(sub_path,
                                      static_controller_callback_factory(os_dependant_sub_path,
                                                                         static_path_string)).build()
                if sub_path.endswith("index.html") or sub_path.endswith("index.htm"):
                    sub_path = sub_path.replace("index.html", "")
                    sub_path = sub_path.replace("index.htm", "")
                    controller_mapper.get(sub_path,
                                          static_controller_callback_factory(os_dependant_sub_path,
                                                                             static_path_string)).build()
