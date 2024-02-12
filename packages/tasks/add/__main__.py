from lib import db


def main(event, context):
    method = event['http']['method']
    if method != 'POST':
        return {"statusCode": 400, "body": {"errors": "Method not supported"}}
    try:
        conn = db.connect()
        desc = event.get("payload")
        insert_query = "insert into task (description) values (%s)"
        insert_params = (desc,)
        cursor = conn.cursor()
        cursor.execute(insert_query, insert_params)
        conn.commit()
        cursor.close()
        response = {"payload": {"id": cursor.lastrowid, "description": desc}}
        print(f"Response processed for function: {context.activation_id}")
        return {"body": response, "statusCode": 200}
    except Exception as e:
        return {"statusCode": 400, "body": {"errors": str(e)}}
