import db
import rabbit
import utils
import web
from utils.applicationContext import ApplicationContext
from web.lib.bottleService import BottleService

ctx = None


def main():
    global ctx
    ctx = ApplicationContext(utils,
                             db,
                             web,
                             rabbit)
    bottle_service: BottleService = ctx.resolve(BottleService)
    bottle_service.run()


if __name__ == '__main__':
    main()
