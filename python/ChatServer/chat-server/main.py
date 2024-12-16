from configparser import ConfigParser

import utils
import web

from applicationContext import ApplicationContext
from web.lib.bottleService import BottleService


def main():



    ctx = ApplicationContext(web,utils)
    bottleService: BottleService = ctx.resolve(BottleService)
    bottleService.run()

if __name__ == '__main__':
    main()