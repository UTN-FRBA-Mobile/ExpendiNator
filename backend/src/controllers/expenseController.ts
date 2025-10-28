import { Request, Response } from "express";
import { ExpenseModel } from "../models/Expense.js";

export const ExpenseController = {
  async getAll(req: Request, res: Response) {
    try {
      const userId = req.userId!;
      const expenses = await ExpenseModel.getAllByUser(userId);
      res.json(expenses);
    } catch (err) {
      console.error(err);
      res.status(500).json({ error: "Error al obtener los gastos" });
    }
  },

  async create(req: Request, res: Response) {
    try {
      const { title, amount, date, category_id } = req.body;
      if (!title || !amount || !date) {
        return res
          .status(400)
          .json({ error: "Título, monto y fecha son requeridos" });
      }

      const expense = await ExpenseModel.create({
        user_id: req.userId!,
        title,
        amount,
        date,
        category_id: category_id || null,
      });

      res.status(201).json(expense);
    } catch (err) {
      console.error(err);
      res.status(500).json({ error: "Error al crear el gasto" });
    }
  },

  async remove(req: Request, res: Response) {
    try {
      const id = Number(req.params.id);
      const ok = await ExpenseModel.remove(id, req.userId!);
      if (!ok) return res.status(404).json({ error: "Gasto no encontrado" });
      res.json({ message: "Gasto eliminado" });
    } catch (err) {
      console.error(err);
      res.status(500).json({ error: "Error al eliminar el gasto" });
    }
  },

  async update(req: Request, res: Response) {
    try {
      const id = Number(req.params.id);
      const userId = req.userId!;
      const { title, amount, date, category_id } = req.body;

      if (!title || typeof amount !== "number" || !date) {
        return res
          .status(400)
          .json({ error: "title, amount y date son requeridos" });
      }

      const existing = await ExpenseModel.findByIdForUser(id, userId);
      if (!existing)
        return res.status(404).json({ error: "Gasto no encontrado" });

      const updated = await ExpenseModel.update({
        id,
        user_id: userId,
        title,
        amount,
        date,
        category_id: category_id ?? null,
      });

      if (!updated)
        return res
          .status(404)
          .json({ error: "No se pudo actualizar el gasto" });
      return res.json(updated);
    } catch (err) {
      console.error(err);
      return res.status(500).json({ error: "Error al actualizar el gasto" });
    }
  },
};
