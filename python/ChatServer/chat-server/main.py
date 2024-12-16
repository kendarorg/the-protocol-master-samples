import web
from ApplicationContext import ApplicationContext
from web.lib.bottleService import BottleService


def main():
    ctx = ApplicationContext(web)
    bottleService: BottleService = ctx.resolve(BottleService)
    bottleService.run(host='', port=80, debug=True)


if __name__ == '__main__':
    main()