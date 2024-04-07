import json
from firebase_functions import https_fn as http
import os
import importlib
from pymongo.results import InsertOneResult
from bson import json_util
db_client = importlib.import_module("lib.db_client", os.getcwd())


@http.on_request()
def add(req: http.Request) -> http.Response:
    method = req.method
    if method != 'POST':
        return http.Response(json.dumps({"errors": "Method not supported"}), status=400)
    try:
        conn = db_client.connect()
        db = conn.get_database("dev")
        coll = db.get_collection("tasks")

        payload = req.get_json()
        desc = payload["description"]
        result: InsertOneResult = coll.insert_one({"description": desc})
        object_id = json.loads(json_util.dumps(result.inserted_id, json_options=json_util.RELAXED_JSON_OPTIONS))
        response = {"payload": {**object_id, "description": desc}}
        return http.Response(json.dumps(response), status=200, mimetype='application/json')
    except Exception as e:
        return http.Response(json.dumps({"errors": str(e)}), status=400)
