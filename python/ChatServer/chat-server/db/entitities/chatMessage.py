from autowired import component
from peewee import CharField, IntegerField

from db.lib.entity import Entity, EntityFactory


class ChatMessage(Entity):
    name = CharField(max_length=255)
    special_move = CharField(max_length=255, null=True)
    ss_level = IntegerField(null=True)
    eye_color = CharField(max_length=255, null=True)


@component
class ChatMessageFactory(EntityFactory):
    # noinspection PyMethodMayBeStatic
    def build_entity(self):
        return ChatMessage()
