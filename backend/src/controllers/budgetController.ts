import { Request, Response } from "express";
import { Budget, BudgetModel, BudgetPeriod } from "../models/Budget.js";

export const BudgetController = {
  async getAll(req: Request, res: Response) {
    try {
      const budgets = await BudgetModel.getAllByUser(req.userId!);
      res.json(budgets);
    } catch (err) {
      console.error(err);
      res.status(500).json({ error: "Error al obtener presupuestos" });
    }
  },

  async create(req: Request, res: Response) {
    try {
      const { category_id, limit_amount, period, start_date, end_date } =
        req.body;

      if (
        !category_id ||
        !limit_amount ||
        !period ||
        !start_date ||
        !end_date
      ) {
        return res.status(400).json({
          error:
            "category_id, limit_amount, period, start_date y end_date son requeridos",
        });
      }

      const validPeriods: BudgetPeriod[] = ["MONTHLY", "WEEKLY", "YEARLY"];
      if (!validPeriods.includes(period)) {
        return res
          .status(400)
          .json({ error: "period inválido (MONTHLY|WEEKLY|YEARLY)" });
      }

      const budget: Budget = {
        user_id: req.userId!,
        category_id: Number(category_id),
        limit_amount: Number(limit_amount),
        period,
        start_date: String(start_date),
        end_date: String(end_date),
      };

      const created = await BudgetModel.create(budget);
      res.status(201).json(created);
    } catch (err) {
      console.error(err);
      res.status(500).json({ error: "Error al crear presupuesto" });
    }
  },

  async update(req: Request, res: Response) {
    try {
      const id = Number(req.params.id);
      const { category_id, limit_amount, period, start_date, end_date } =
        req.body;

      if (
        !category_id ||
        !limit_amount ||
        !period ||
        !start_date ||
        !end_date
      ) {
        return res.status(400).json({
          error:
            "category_id, limit_amount, period, start_date y end_date son requeridos",
        });
      }

      const existing = await BudgetModel.findByIdForUser(id, req.userId!);
      if (!existing)
        return res.status(404).json({ error: "Presupuesto no encontrado" });

      const validPeriods: BudgetPeriod[] = ["MONTHLY", "WEEKLY", "YEARLY"];
      if (!validPeriods.includes(period)) {
        return res
          .status(400)
          .json({ error: "period inválido (MONTHLY|WEEKLY|YEARLY)" });
      }

      const updated = await BudgetModel.update({
        id,
        user_id: req.userId!,
        category_id: Number(category_id),
        limit_amount: Number(limit_amount),
        period,
        start_date: String(start_date),
        end_date: String(end_date),
      });

      if (!updated)
        return res
          .status(404)
          .json({ error: "No se pudo actualizar el presupuesto" });
      res.json(updated);
    } catch (err) {
      console.error(err);
      res.status(500).json({ error: "Error al actualizar presupuesto" });
    }
  },

  async remove(req: Request, res: Response) {
    try {
      const id = Number(req.params.id);
      const ok = await BudgetModel.remove(id, req.userId!);
      if (!ok)
        return res.status(404).json({ error: "Presupuesto no encontrado" });
      res.json({ message: "Presupuesto eliminado" });
    } catch (err) {
      console.error(err);
      res.status(500).json({ error: "Error al eliminar presupuesto" });
    }
  },
};
