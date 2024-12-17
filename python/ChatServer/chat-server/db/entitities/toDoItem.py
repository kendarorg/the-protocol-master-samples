from datetime import datetime

from autowired import component
from peewee import CharField, DateTimeField, BooleanField, TextField, IntegerField

from db.lib.entity import Entity, EntityFactory


class ToDoItem(Entity):
    shortDescription = CharField(max_length=255)
    description = TextField()
    priority = IntegerField()
    done = BooleanField(default=False)
    before = DateTimeField()
    timestamp = DateTimeField(default=datetime.now)


@component
class ToDoItemFactory(EntityFactory):
    # noinspection PyMethodMayBeStatic
    def build_entity(self):
        return ToDoItem()
