import bcrypt from "bcrypt";
import { Request, Response } from "express";
import jwt from "jsonwebtoken";
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
    await UserModel.create({ email, password: hashed });

    return res.status(201).json({ message: "Usuario registrado con éxito" });
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
