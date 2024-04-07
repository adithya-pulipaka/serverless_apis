import json
from firebase_functions import https_fn as http
import os
import importlib
from pymongo.database import Collection
from pymongo.results import UpdateResult
from bson.objectid import ObjectId
db_client = importlib.import_module("lib.db_client", os.getcwd())


@http.on_request()
def update(req: http.Request) -> http.Response:
    # return None
    method = req.method
    if method != 'PUT':
        return http.Response(json.dumps({"errors": "Method not supported"}), status=400)
    try:
        conn = db_client.connect()
        db = conn.get_database("dev")
        coll: Collection = db.get_collection("tasks")

        payload = req.get_json()
        object_id = req.view_args['path']
        if not object_id:
            return http.Response(json.dumps({"errors": "TaskId doesn't exist"}), status=400, mimetype='application/json')

        result: UpdateResult = coll.update_one({"_id": ObjectId(object_id)}, update={"description": payload["description"], "completed": payload["completed"]})
        if result.modified_count == 1:
            return http.Response(status= 200)
        else:
            return http.Response(status= 500)
    except Exception as e:
        return http.Response(json.dumps({"errors": str(e)}), status=400)
