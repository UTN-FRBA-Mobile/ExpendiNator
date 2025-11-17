import { CategoryModel } from "../models/Category.js";
import { ExpenseModel } from "../models/Expense.js";
import { sanitizeDateInput } from "../utils/date.js";
export const OcrConfirmController = {
    async confirm(req, res) {
        try {
            const userId = req.userId;
            const items = req.body.items;
            if (!Array.isArray(items) || items.length === 0) {
                return res.status(400).json({ error: "items es requerido y debe ser una lista" });
            }
            const created = [];
            for (const it of items) {
                const title = it.title?.trim();
                const amount = Number(it.amount);
                const categoryName = it.categoryName || it.category || null;
                const categoryId = it.categoryId ?? null;
                const dateCandidate = it.date || new Date().toISOString();
                let normalizedDate;
                try {
                    normalizedDate = sanitizeDateInput(dateCandidate);
                }
                catch (err) {
                    continue;
                }
                if (!title || isNaN(amount))
                    continue;
                let categoryIdToPersist = null;
                if (categoryId !== null && categoryId !== undefined) {
                    const cat = await CategoryModel.findById(userId, Number(categoryId));
                    if (cat) {
                        categoryIdToPersist = cat.id;
                    }
                }
                if (!categoryIdToPersist && categoryName) {
                    const cat = await CategoryModel.findByName(userId, categoryName);
                    if (cat)
                        categoryIdToPersist = cat.id;
                }
                const newExpense = await ExpenseModel.create({
                    user_id: userId,
                    title,
                    amount,
                    date: normalizedDate,
                    category_id: categoryIdToPersist
                });
                created.push(newExpense);
            }
            return res.json({ created });
        }
        catch (err) {
            console.error(err);
            return res.status(500).json({ error: "Error al confirmar gastos del OCR" });
        }
    }
};
