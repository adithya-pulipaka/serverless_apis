import { budget_db } from "../setup/db.js";
import { ObjectId } from "mongodb";

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
      res.status(200).json({ payload: { ...payload } });
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

export const update = async (req, res) => {
  try {
    const coll = await getCollection();
    const body = req.body;
    const tranId = req.params.tranId;
    if (!tranId) {
      res.status(400).json({ errors: "TranId is mandatory" });
    }
    const payload = {
      ...body,
    };
    const updateQuery = {
      $set: {
        description: payload["description"],
        completed: payload["completed"] || false,
        amount: payload["amount"],
      },
    };
    const result = await coll.updateOne(
      { _id: ObjectId.createFromHexString(tranId) },
      updateQuery
    );
    console.log(result);
    if (result.modifiedCount == 1) {
      res.json({ payload: "Updated Record" });
    } else {
      res.status(500).json({ errors: "unable to update record" });
    }
  } catch (ex) {
    res.status(500).json({ errors: "unable to update record" });
  }
};

export const delT = async (req, res) => {
  try {
    const coll = await getCollection();
    const tranId = req.params.tranId;
    if (!tranId) {
      res.status(400).json({ errors: "TranId is mandatory" });
    }
    const result = await coll.deleteOne({
      _id: ObjectId.createFromHexString(tranId),
    });
    if (result.deletedCount == 0) {
      res.status(400).json({ errors: "TranId doesnt exist" });
    } else {
      res.json({ payload: "Deleted Record" });
    }
  } catch (ex) {
    res.status(500).json({ errors: "unable to delete record" });
  }
};
