import express from "express";
import { BudgetController } from "../controllers/budgetController.js";
import { verifyToken } from "../middleware/verifyToken.js";

export const budgetRouter = express.Router();

budgetRouter.get("/", verifyToken, BudgetController.getAll);
budgetRouter.post("/", verifyToken, BudgetController.create);
budgetRouter.put("/:id", verifyToken, BudgetController.update);
budgetRouter.delete("/:id", verifyToken, BudgetController.remove);
budgetRouter.get("/usage", verifyToken, BudgetController.usage);
