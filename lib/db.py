import mysql.connector
import os

db_connected = False
db_conn = None


def connect():
    global db_connected
    global db_conn
    if not db_connected:
        print("connecting to DB")
        db_conn = mysql.connector.connect(
            host=os.getenv("DB_HOST"),
            user=os.getenv("DB_USERNAME"),
            passwd=os.getenv("DB_PASSWORD"),
            db=os.getenv("DB_NAME"),
        )
        db_connected = True
    return db_conn
