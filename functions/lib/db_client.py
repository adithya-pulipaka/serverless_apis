import os
from pymongo import MongoClient

db_connected = False
db_conn = None


def connect():
    global db_connected
    global db_conn
    if not db_connected:
        print("connecting to DB")
        db_conn = MongoClient(
            host=os.environ.get("ATLAS_URI"),
        )
        db_connected = True
    return db_conn
