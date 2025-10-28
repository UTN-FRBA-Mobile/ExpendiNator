import express from "express";
import { CategoryController } from "../controllers/categoryController.js";
import { verifyToken } from "../middleware/verifyToken.js";

export const categoryRouter = express.Router();

categoryRouter.get("/", verifyToken, CategoryController.getAll);
categoryRouter.post("/", verifyToken, CategoryController.create);
categoryRouter.delete("/:id", verifyToken, CategoryController.remove);
