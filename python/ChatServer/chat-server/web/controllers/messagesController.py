from autowired import component

from db.entitities.chatMessage import ChatMessage
from web.lib.controller import Controller
from web.lib.decorators import qroute


@component
class MessagesController(Controller):

    @qroute("/api/messages/<user>")
    def show_messages(self, user):
        data = list(ChatMessage.select().dicts())
        from bottle import response
        from json import dumps
        response.content_type = 'application/json'
        return dumps(data)
