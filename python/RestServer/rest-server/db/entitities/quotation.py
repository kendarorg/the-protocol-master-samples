from autowired import component
from peewee import CharField, DateTimeField, IntegerField, DoubleField

from db.lib.entity import Entity, EntityFactory


class Quotation(Entity):
    symbol = CharField(max_length=5)
    date = DateTimeField()
    price = DoubleField()
    volume = IntegerField()


@component
class QuotationFactory(EntityFactory):
    # noinspection PyMethodMayBeStatic
    def build_entity(self):
        return Quotation()
