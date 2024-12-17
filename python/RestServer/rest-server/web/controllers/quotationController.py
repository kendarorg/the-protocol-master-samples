import json
from json import dumps

from autowired import component
from bottle import request, abort
from bottle import response
from playhouse.shortcuts import dict_to_model, model_to_dict

from db.entitities.quotation import Quotation
from utils.jsonMapper import to_json_with_iso_dates
from web.lib.controller import Controller
from web.lib.decorators import qroute


@component
class MessagesController(Controller):

    @qroute("/api/quotation")
    def get_all(self):
        data = list(Quotation.select().order_by(Quotation.priority.desc()).dicts())
        response.content_type = 'application/json'
        return to_json_with_iso_dates(data)

    @qroute("/api/quotation", verb="POST")
    def create(self):
        current: Quotation = dict_to_model(Quotation,json.loads(request.body.getvalue().decode('utf-8')))
        current.save()
        response.content_type = 'application/json'
        return to_json_with_iso_dates(model_to_dict(current))

    @qroute("/api/quotation/<index>")
    def get_by_id(self, index):
        for item in Quotation.select().where(Quotation.id == index):
            response.content_type = 'application/json'
            return to_json_with_iso_dates(model_to_dict(item))
        abort(404, "Item not found.")

    @qroute("/api/quotation/<index>", verb="PUT")
    def update(self, index):
        updated: Quotation = dict_to_model(Quotation,json.loads(request.body.getvalue().decode('utf-8')))
        for old in Quotation.select().where(Quotation.id == index):
            old.shortDescription = updated.shortDescription
            old.description = updated.description
            old.priority = updated.priority
            old.done = updated.done
            old.before = updated.before
            old.save()
            response.content_type = 'application/json'
            return to_json_with_iso_dates(model_to_dict(old))
        abort(404, "Item not found.")

    @qroute("/api/quotation/<index>", verb="DELETE")
    def delete_by_id(self, index):
        try:
            for item in Quotation.select().where(Quotation.id == index):
                result = to_json_with_iso_dates(model_to_dict(item))
                item.delete_instance()
                response.content_type = 'application/json'
                return result
        except:
            return ''