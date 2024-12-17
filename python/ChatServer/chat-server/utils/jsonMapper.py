from datetime import datetime
from json import dumps


def datetime_handler(x):
    if isinstance(x, datetime):
        return x.isoformat()
    raise TypeError("Unknown type")


def to_json_with_iso_dates(to_serialize):
    return dumps(to_serialize, default=datetime_handler)
