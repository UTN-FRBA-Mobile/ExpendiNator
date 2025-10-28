import express from "express";
import { AuthController } from "../controllers/authController.js";

export const authRouter = express.Router();

authRouter.post("/register", AuthController.register);
authRouter.post("/login", AuthController.login);
