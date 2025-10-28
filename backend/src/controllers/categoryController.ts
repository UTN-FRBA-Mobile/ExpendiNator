import { Request, Response } from "express";
import { CategoryModel } from "../models/Category.js";

export const CategoryController = {
  async getAll(req: Request, res: Response) {
    try {
      const userId = req.userId!;
      const categories = await CategoryModel.getAllByUser(userId);
      res.json(categories);
    } catch (err) {
      console.error(err);
      res.status(500).json({ error: "Error al obtener las categorías" });
    }
  },

  async create(req: Request, res: Response) {
    try {
      const { name, color, keywords } = req.body;
      if (!name) return res.status(400).json({ error: "El nombre es requerido" });

      const category = await CategoryModel.create({
        user_id: req.userId!,
        name,
        color,
        keywords: Array.isArray(keywords) ? keywords : [],
      });

      res.status(201).json(category);
    } catch (err) {
      console.error(err);
      res.status(500).json({ error: "Error al crear la categoría" });
    }
  },

  async remove(req: Request, res: Response) {
    try {
      const id = Number(req.params.id);
      const ok = await CategoryModel.remove(id, req.userId!);
      if (!ok) return res.status(404).json({ error: "Categoría no encontrada" });
      res.json({ message: "Categoría eliminada" });
    } catch (err) {
      console.error(err);
      res.status(500).json({ error: "Error al eliminar la categoría" });
    }
  },
};
