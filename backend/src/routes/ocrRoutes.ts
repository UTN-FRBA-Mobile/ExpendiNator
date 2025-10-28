import express from "express";
import { MockOcrSimpleController } from "../controllers/ocrController.js";
import { verifyToken } from "../middleware/verifyToken.js";

export const ocrRouter = express.Router();

ocrRouter.get("/mock", verifyToken, MockOcrSimpleController.parse);
