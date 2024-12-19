from autowired import component
from peewee import MySQLDatabase

from db.lib.entity import EntityFactory
from utils.appSettings import AppSettings
from utils.autostart import Autostart


@component
class DbConnection(Autostart):

    def __init__(self, app_settings: AppSettings, entities: list[EntityFactory]):
        try:
            if entities is None:
                return
            db_connection = MySQLDatabase(app_settings.db_database, host=app_settings.db_host, port=app_settings.db_port,
                                          user=app_settings.db_user, password=app_settings.db_password)
            db_connection.connect()
            for entity in entities:
                basic_entity = entity.build_entity()
                if basic_entity is None:
                    continue
                basic_entity.get_meta().set_database(db_connection)
                if app_settings.db_create_tables:
                    db_connection.create_tables([basic_entity])

        except Exception as e:
            print("Unable to initialize DbConnection ",e)