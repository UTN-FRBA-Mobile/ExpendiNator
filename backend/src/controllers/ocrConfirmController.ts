import { Request, Response } from "express";
import { CategoryModel } from "../models/Category.js";
import { ExpenseModel } from "../models/Expense.js";

export const OcrConfirmController = {
  async confirm(req: Request, res: Response) {
    try {
      const userId = req.userId!;
      const items = req.body.items;

      if (!Array.isArray(items) || items.length === 0) {
        return res.status(400).json({ error: "items es requerido y debe ser una lista" });
      }

      const created = [];

      for (const it of items) {
        const title = it.title?.trim();
        const amount = Number(it.amount);
        const categoryName = it.category || null;
        const date = it.date || new Date().toISOString().slice(0, 10);

        if (!title || isNaN(amount)) continue;

        let categoryId = null;
        if (categoryName) {
          const cat = await CategoryModel.findByName(userId, categoryName);
          if (cat) categoryId = cat.id;
        }

        const newExpense = await ExpenseModel.create({
          user_id: userId,
          title,
          amount,
          date,
          category_id: categoryId
        });

        created.push(newExpense);
      }

      return res.json({ created });

    } catch (err) {
      console.error(err);
      return res.status(500).json({ error: "Error al confirmar gastos del OCR" });
    }
  }
};
