import express from "express";
import { ExpenseController } from "../controllers/expenseController.js";
import { verifyToken } from "../middleware/verifyToken.js";

export const expenseRouter = express.Router();

expenseRouter.get("/", verifyToken, ExpenseController.getAll);
expenseRouter.post("/", verifyToken, ExpenseController.create);
expenseRouter.put("/:id", verifyToken, ExpenseController.update);
expenseRouter.delete("/:id", verifyToken, ExpenseController.remove);
expenseRouter.get("/summary", verifyToken, ExpenseController.summary);
