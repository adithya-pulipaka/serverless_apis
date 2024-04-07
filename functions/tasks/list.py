# sory by keywords translated internally like Featured,High2Low,BestSelling
import json
from firebase_functions import https_fn as http
import os
import importlib
from pymongo.cursor import Cursor
from pymongo.database import Collection
from bson import json_util

db_client = importlib.import_module("lib.db_client", os.getcwd())


@http.on_request()
def list(req: http.Request) -> http.Response:
    method = req.method
    if method != 'GET':
        return http.Response(json.dumps({"errors": "Method not supported"}), status=400)
    try:
        conn = db_client.connect()
        db = conn.get_database("dev")
        coll: Collection = db.get_collection("tasks")
        page = req.args.get("page", default=1, type=int)  # offset
        size = req.args.get("size", default=5, type=int)  # limit
        #     default_sort_query = "created_at,desc"
        #     sort = event.get("sort", default_sort_query)  # sort
        #     sort_query = ' '.join(sort.split(","))
        if page < 1 or size < 1:
            return http.Response(json.dumps({"errors": "page/size must be greater than 0"}), status=400)

        # id_deleted=0 && completed=0, sort by created_at desc => same as _id desc
        result: Cursor = coll.find().sort({"_id": -1}).limit(size).skip((page - 1) * size)
        response = []
        for row in result:
            object_id = json.loads(json_util.dumps(row.get("_id"), json_options=json_util.RELAXED_JSON_OPTIONS))
            response.append({**object_id, "description": row.get("description"), "completed": row.get("completed")})
        return http.Response(json.dumps({"payload": response}), status=200, mimetype='application/json')
    except Exception as e:
        return http.Response(json.dumps({"errors": str(e)}), status=400)
