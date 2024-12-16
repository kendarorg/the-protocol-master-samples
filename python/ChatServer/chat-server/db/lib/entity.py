from peewee import Model

global ctx


class EntityFactory:
    def build_entity(self):
        pass


class Entity(Model):
    def get_meta(self):
        return self._meta

    class Meta:
        database = None
