import web
import argparse
from ApplicationContext import ApplicationContext
from web.lib.bottleService import BottleService


def main():
    parser = argparse.ArgumentParser(description='Start the website')
    parser.add_argument('--port', metavar='port', required=False,
                        help='The listening port',default="8080")

    args = parser.parse_args()
    port = int(args.port)
    ctx = ApplicationContext(web)
    bottleService: BottleService = ctx.resolve(BottleService)
    bottleService.run(host='', port=port, debug=True)


if __name__ == '__main__':
    main()