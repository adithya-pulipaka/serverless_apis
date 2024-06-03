import json
from firebase_functions import https_fn as http
import os
import importlib
from pymongo.results import InsertOneResult
from bson import json_util
from datetime import datetime
db_client = importlib.import_module("lib.db_client", os.getcwd())

# Schema
# name: taskDetails.item,
# completed: false,
# created: new
# Date().toISOString(),
# active: true,
# dueDate: taskDetails.dueDate,
# priority: taskDetails.priority,
# tags: taskDetails.tags,

@http.on_request()
def add(req: http.Request) -> http.Response:
    method = req.method
    if method != 'POST':
        return http.Response(json.dumps({"errors": "Method not supported"}), status=400)
    try:
        conn = db_client.connect()
        db = conn.get_database("dev")
        coll = db.get_collection("tasks")

        req_body = req.get_json()
        payload = {**req_body, "completed": False, "created": datetime.utcnow(), "active": True}
        result: InsertOneResult = coll.insert_one(payload)
        if not result.inserted_id:
            return http.Response(json.dumps({"errors": "Insert Failed"}), status=500)
        print(payload)
        print(type(payload["created"]))
        payload = {**payload, "created": payload["created"].isoformat()}
        formatted_resp = json.loads(json_util.dumps(payload, json_options=json_util.RELAXED_JSON_OPTIONS))
        response_dict = {(name if name != "_id" else "id"): (val if name != "_id" else val["$oid"]) for name, val in formatted_resp.items()}
        response = {"payload": {**response_dict}}
        return http.Response(json.dumps(response), status=200, mimetype='application/json')
    except Exception as e:
        return http.Response(json.dumps({"errors": str(e)}), status=400)
