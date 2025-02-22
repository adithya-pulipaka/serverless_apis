import { MongoClient } from "mongodb";

const uri = process.env.ATLAS_URI;

let connected = false;
let connection;
export const connect = async () => {
  try {
    if (!connected) {
      connection = new MongoClient(uri);
      console.log("Database connected!");
      connected = true;
      return connection;
    }
    return connection;
  } catch (error) {
    console.error("Error connecting to database:", error);
    throw error;
  }
};

export const budget_db = async () => {
  const connection = await connect();
  const db = connection.db(process.env.BUDGET_DB);
  return db;
};
