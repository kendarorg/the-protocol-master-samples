import jsons
from autowired import component
from bottle import abort
from bottle import response
from playhouse.shortcuts import model_to_dict

from db.entitities.quotation import Quotation
from web.lib.controller import Controller
from web.lib.decorators import qroute


@component
class MessagesController(Controller):

    @qroute("/api/quotation/symbols")
    def get_all(self):
        data = list(Quotation.select(Quotation.symbol).group_by(Quotation.symbol).dicts())
        response.content_type = 'application/json'
        return jsons.dumps(data)

    @qroute("/api/quotation/symbols", verb='DELETE')
    def delete_all(self):
        Quotation.delete()
        response.content_type = 'application/json'
        return "OK"

    @qroute("/api/quotation/quotes/<identifier>")
    def get_by_id(self, identifier: str):
        data = list(Quotation.select().
                    where(Quotation.symbol == identifier).
                    order_by(Quotation.date.desc()))
        response.content_type = 'application/json'
        return jsons.dumps(data)

    @qroute("/api/quotation/quote/<identifier>")
    def get_by_id(self, identifier: str):
        data = list(Quotation.select().
                    where(Quotation.symbol == identifier).
                    order_by(Quotation.date.desc()).dicts())
        response.content_type = 'application/json'
        for item in data:
            return jsons.dumps(item)
        abort(404, "Item not found.")
