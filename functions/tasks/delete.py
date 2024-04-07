import json
from firebase_functions import https_fn as http
import os
import importlib
from pymongo.database import Collection
from pymongo.results import DeleteResult
from bson.objectid import ObjectId

db_client = importlib.import_module("lib.db_client", os.getcwd())


@http.on_request()
def delete(req: http.Request) -> http.Response:
    method = req.method
    if method != 'DELETE':
        return http.Response(json.dumps({"errors": "Method not supported"}), status=500)
    try:
        conn = db_client.connect()
        db = conn.get_database("dev")
        coll: Collection = db.get_collection("tasks")

        object_id = req.view_args['path']
        result: DeleteResult = coll.delete_one({"_id": ObjectId(object_id)})
        print(result.deleted_count)
        if result.deleted_count == 0:
            return http.Response(json.dumps({"errors": "TaskId doesn't exist"}), status=400, mimetype='application/json')
        return http.Response(status=200)
    except Exception as e:
        return http.Response(json.dumps({"errors": str(e)}), status=400)
