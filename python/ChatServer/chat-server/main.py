import db
import utils
import web
from applicationContext import ApplicationContext
from db.lib.dbConnection import DbConnection
from web.lib.bottleService import BottleService

ctx = None


def main():
    global ctx
    ctx = ApplicationContext(utils,
                             db,
                             web)
    bottle_service: BottleService = ctx.resolve(BottleService)
    connection: DbConnection = ctx.resolve(DbConnection)
    bottle_service.run()


if __name__ == '__main__':
    main()
