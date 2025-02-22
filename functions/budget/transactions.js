import { budget_db } from "../setup/db.js";

// # Schema
// # name: taskDetails.item,
// # completed: false,
// # created: new
// # Date().toISOString(),
// # active: true,
// # dueDate: taskDetails.dueDate,
// # priority: taskDetails.priority,
// # tags: taskDetails.tags,

const getCollection = async () => {
  const db = await budget_db();
  const coll = db.collection("transactions");
  return coll;
};

export const add = async (req, res) => {
  try {
    const coll = await getCollection();
    const body = req.body;
    const payload = {
      ...body,
      completed: false,
      created: Date.now(),
      active: true,
    };
    const response = await coll.insertOne(payload);
    if (!response.acknowledged) {
      res.status(500).json({ errors: "Insert Failed" });
    } else {
      res.status(200).json({ payload: { ...body, id: response.insertedId } });
    }
  } catch (ex) {
    res.status(500).json({ errors: "Insert Failed" });
  }
};

export const list = async (req, res) => {
  try {
    const coll = await getCollection();
    let { page, size } = { ...req.query };
    page = +page || 1; // offset
    size = +size || 5; // limit
    console.log(typeof size);
    const options = {
      sort: { _id: 1 },
      limit: size,
      skip: page,
    };
    const cursor = await coll.find({}, options);
    let results = [];
    for await (const tran of cursor) {
      results.push(tran);
    }
    res.json({ payload: results });
  } catch (ex) {
    res.status(500).json({ errors: "unable to retrieve records" });
  }
};
