import { NextFunction, Request, Response } from "express";
import jwt from "jsonwebtoken";

const JWT_SECRET = process.env.JWT_SECRET || "dev-secret";

export function verifyToken(req: Request, res: Response, next: NextFunction) {
  const header = req.headers.authorization;

  if (!header) {
    return res.status(401).json({ error: "Token requerido" });
  }

  // Espera un header tipo: Authorization: Bearer <token>
  const token = header.split(" ")[1];
  if (!token) {
    return res.status(401).json({ error: "Formato de autorización inválido" });
  }

  try {
    const decoded = jwt.verify(token, JWT_SECRET) as { id: number };
    req.userId = decoded.id;

    next(); // continuar al controller o proximo middleware
  } catch (err) {
    console.error("Error de token:", err);
    return res.status(401).json({ error: "Token inválido o expirado" });
  }
}
