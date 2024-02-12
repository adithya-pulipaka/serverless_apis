from lib import db


def main(event, context):
    method = event['http']['method']
    if method != 'PUT':
        return {"statusCode": 400, "body": {"errors": "Method not supported"}}
    try:
        conn = db.connect()
        task_id = int(event['http'].get("path")[1:])
        task = event.get("payload")
        if task_id == -1:
            return {"statusCode": 400, "body": {"errors": "TaskId doesn't exist"}}
        update_query = "update task set description = %s, completed = %s where task_id = %s"
        update_params = (task['description'], task['completed'], task_id,)
        cursor = conn.cursor()
        cursor.execute(update_query, update_params)
        conn.commit()
        cursor.close()
        print(f"Response processed for function: {context.activation_id}")
        if cursor.rowcount == 1:
            return {"statusCode": 200}
        else:
            return {"statusCode": 500}
    except Exception as e:
        return {"statusCode": 400, "body": {"errors": str(e)}}
