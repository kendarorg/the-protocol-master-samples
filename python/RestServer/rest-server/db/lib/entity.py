from peewee import Model

global ctx


class EntityFactory:
    def build_entity(self):
        pass


class Entity(Model):

    def __init__(self, dictionary = None):
        Model.__init__(self)
        if not dictionary is None:
            for key in dictionary:
                setattr(self, key, dictionary[key])

    def __repr__(self):
        return "<Entity: %s>" % self.__dict__

    def get_meta(self):
        return self._meta

    class Meta:
        database = None
