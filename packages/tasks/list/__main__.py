from lib import db


# sory by keywords translated internally like Featured,High2Low,BestSelling
def main(event, context):
    method = event['http']['method']
    if method != 'GET':
        return {"statusCode": 400, "body": {"errors": "Method not supported"}}
    try:
        conn = db.connect()
        page = int(event.get("page", 1))  # offset
        size = int(event.get("size", 5))  # limit
        default_sort_query = "created_at,desc"
        sort = event.get("sort", default_sort_query)  # sort
        sort_query = ' '.join(sort.split(","))
        if page < 1 or size < 1:
            return {"statusCode": 400, "body": {"errors": "page/size must be greater than 0"}}
        select_query = ("select task_id,description,completed from task where is_deleted = 0 and completed = 0 "
                        f"order by {sort_query} limit %s offset %s")
        select_params = (size, (page-1)*size,)
        cursor = conn.cursor()
        cursor.execute(select_query, select_params)
        tasks = list()
        for (task_id, description, completed) in cursor:
            tasks.append({"task_id": task_id, "description": description, "completed": completed})
        cursor.close()
        response = {"payload": tasks}
        print(f"Response processed for function: {context.activation_id}")
        return {"body": response, "statusCode": 200}
    except Exception as e:
        return {"statusCode": 400, "body": {"errors": str(e)}}
