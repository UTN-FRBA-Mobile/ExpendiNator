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

  async usage(req: Request, res: Response) {
    try {
      const activeOnly =
        String(req.query.active || "").toLowerCase() === "true";
      const from = req.query.from;
      const to = req.query.to;

      // Validación simple de fechas (YYYY-MM-DD)
      const fromStr = isValidISODate(from) ? from : null;
      const toStr = isValidISODate(to) ? to : null;

      // Si vienen ambas, validar orden (comparación lexicográfica sirve para YYYY-MM-DD)
      if (fromStr && toStr && fromStr > toStr) {
        return res
          .status(400)
          .json({ error: "`from` no puede ser mayor que `to`" });
      }

      const rows = await BudgetModel.getUsageByUser(req.userId!, {
        activeOnly,
        from: fromStr,
        to: toStr,
      });

      const result = rows.map((r) => {
        const percentUsed = r.limit_amount > 0 ? r.spent / r.limit_amount : 0;
        const remaining = Math.max(0, r.limit_amount - r.spent);
        const over = r.spent > r.limit_amount;

        return {
          budget_id: r.id,
          limit_amount: r.limit_amount,
          period: r.period,
          // Ventana original del budget (informativa)
          start_date: r.start_date,
          end_date: r.end_date,
          // Ventana efectiva aplicada para el cálculo (override de query si corresponde)
          effective_start_date: r.eff_start_date,
          effective_end_date: r.eff_end_date,

          spent: r.spent,
          percent_used: Number(percentUsed.toFixed(4)),
          remaining,
          over,

          category: {
            id: r.category_id,
            name: r.category_name,
            color: r.category_color,
          },
        };
      });

      res.json(result);
    } catch (err) {
      console.error(err);
      res
        .status(500)
        .json({ error: "Error al calcular el uso de presupuestos" });
    }
  },
};

function isValidISODate(s: unknown): s is string {
  return typeof s === "string" && /^\d{4}-\d{2}-\d{2}$/.test(s);
}