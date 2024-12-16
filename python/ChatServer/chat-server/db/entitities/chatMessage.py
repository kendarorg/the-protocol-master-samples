from datetime import datetime

from autowired import component
from peewee import CharField, DateTimeField, BooleanField

from db.lib.entity import Entity, EntityFactory


class ChatMessage(Entity):
    creator = CharField(max_length=255)
    destination = CharField(max_length=255)
    content = CharField(max_length=255)
    read = BooleanField()
    timestamp = DateTimeField(default=datetime.now)


@component
class ChatMessageFactory(EntityFactory):
    # noinspection PyMethodMayBeStatic
    def build_entity(self):
        return ChatMessage()
