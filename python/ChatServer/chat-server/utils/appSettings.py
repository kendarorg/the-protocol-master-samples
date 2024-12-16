import argparse
from configparser import ConfigParser

from autowired import component


@component
class AppSettings:
    port: int = 8080

    def __init__(self):
        parser = argparse.ArgumentParser(description='Start the website')
        parser.add_argument('--cfg', metavar='config', required=False,
                            help='The config file', default="properties.ini")
        args = parser.parse_args()
        config = ConfigParser()
        config.read(args.cfg)
        self.port = int(config.get('HTTP', 'port'))
        self.debug = bool(config.get('HTTP', 'debug'))
        self.host = config.get('HTTP', 'host')