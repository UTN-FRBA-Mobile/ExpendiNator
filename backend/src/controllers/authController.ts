import bcrypt from "bcrypt";
import { Request, Response } from "express";
import jwt from "jsonwebtoken";
import { DEFAULT_CATEGORIES } from "../config/defaultCategories.js";
import { CategoryModel } from "../models/Category.js";
import { UserModel } from "../models/User.js";

const JWT_SECRET = process.env.JWT_SECRET || "secret";

export const AuthController = {
  async register(req: Request, res: Response) {
    const { email, password } = req.body;
    if (!email || !password)
      return res.status(400).json({ error: "Email y password requeridos" });

    const existing = await UserModel.findByEmail(email);
    if (existing)
      return res.status(400).json({ error: "El usuario ya existe" });

    const hashed = await bcrypt.hash(password, 10);
    const userId = await UserModel.create({ email, password: hashed });

    try {
      for (const category of DEFAULT_CATEGORIES) {
        await CategoryModel.create({
          user_id: userId,
          name: category.name,
          color: category.color,
          keywords: category.keywords,
        });
      }
    } catch (err) {
      console.error("Error al crear categorías por defecto", err);
      await UserModel.deleteById(userId);
      return res.status(500).json({
        error: "No se pudieron crear las categorías iniciales para el usuario",
      });
    }

    const token = jwt.sign({ id: userId }, JWT_SECRET, { expiresIn: "1d" });

    return res.status(201).json({
      token,
      user: { id: userId, email },
    });
  },

  async login(req: Request, res: Response) {
    const { email, password } = req.body;
    const user = await UserModel.findByEmail(email);
    if (!user) return res.status(401).json({ error: "Credenciales inválidas" });

    const match = await bcrypt.compare(password, user.password);
    if (!match)
      return res.status(401).json({ error: "Credenciales inválidas" });

    const token = jwt.sign({ id: user.id }, JWT_SECRET, { expiresIn: "1d" });
    return res.json({ token, user: { id: user.id, email: user.email } });
  },
};
