import express from "express";
import { add, list } from "../budget/transactions.js";

export const app = express();

app.post("/api/transactions/create", add);
app.get("/list", list);
