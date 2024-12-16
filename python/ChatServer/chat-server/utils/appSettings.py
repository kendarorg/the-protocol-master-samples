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
        self.db_host = config.get('MYSQL', 'host')
        self.db_port = int(config.get('MYSQL', 'port'))
        self.db_user = config.get('MYSQL', 'user')
        self.db_password = config.get('MYSQL', 'password')
        self.db_database = config.get('MYSQL', 'database')
        self.db_create_tables = bool(config.get('MYSQL', 'create_tables'))
