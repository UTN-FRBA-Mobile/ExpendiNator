import express from "express";
import { OcrConfirmController } from "../controllers/ocrConfirmController.js";
import { MockOcrSimpleController } from "../controllers/ocrController.js";
import { verifyToken } from "../middleware/verifyToken.js";

export const ocrRouter = express.Router();

ocrRouter.get("/mock", verifyToken, MockOcrSimpleController.parse);

ocrRouter.post("/confirm", verifyToken, OcrConfirmController.confirm);
