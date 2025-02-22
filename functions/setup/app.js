import express from "express";
import { add, delT, list, update } from "../budget/transactions.js";

export const app = express();

app.post("/api/transactions/create", add);
app.get("/api/transactions", list);
app.put("/api/transactions/:tranId", update);
app.delete("/api/transactions/:tranId", delT);
