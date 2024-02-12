from lib import db


def main(event, context):
    method = event['http']['method']
    if method != 'DELETE':
        return {"statusCode": 400, "body": {"errors": "Method not supported"}}
    try:
        conn = db.connect()
        task_id = int(event['http'].get("path")[1:])
        if task_id == -1:
            return {"statusCode": 400, "body": {"errors": "TaskId doesn't exist"}}
        delete_query = "update task set is_deleted=1 where task_id = %s"
        delete_params = (task_id,)
        cursor = conn.cursor()
        cursor.execute(delete_query, delete_params)
        conn.commit()
        cursor.close()
        print(f"Response processed for function: {context.activation_id}")
        return {"statusCode": 200}
    except Exception as e:
        return {"statusCode": 400, "body": {"errors": str(e)}}
