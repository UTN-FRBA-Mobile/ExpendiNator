import { pool } from "../config/db.js";

export interface Expense {
  id?: number;
  user_id: number;
  title: string;
  amount: number;
  date: string;
  category_id?: number | null;
  category?: {
    id: number;
    name: string;
    color?: number;
  } | null;
}

export const ExpenseModel = {
  async getAllByUser(userId: number) {
    const [rows] = await pool.query(
      `SELECT e.id, e.title, e.amount, e.date,
              c.id AS category_id, c.name AS category_name, c.color AS category_color
         FROM expenses e
         LEFT JOIN categories c ON e.category_id = c.id
        WHERE e.user_id = ?
        ORDER BY e.date DESC`,
      [userId]
    );

    return (rows as any[]).map((r) => ({
      id: r.id,
      title: r.title,
      amount: r.amount,
      date: r.date,
      category: r.category_id
        ? { id: r.category_id, name: r.category_name, color: r.category_color }
        : null,
    }));
  },

  async create(expense: Expense) {
    const { user_id, title, amount, date, category_id } = expense;

    const [result] = await pool.query(
      `INSERT INTO expenses (user_id, title, amount, date, category_id)
       VALUES (?, ?, ?, ?, ?)`,
      [user_id, title, amount, date, category_id || null]
    );

    return { id: (result as any).insertId, ...expense };
  },

  async remove(id: number, userId: number) {
    const [result] = await pool.query(
      "DELETE FROM expenses WHERE id = ? AND user_id = ?",
      [id, userId]
    );
    return (result as any).affectedRows > 0;
  },

  async update(expense: Expense) {
    const { id, user_id, title, amount, date, category_id } = expense;

    const [result] = await pool.query(
      `UPDATE expenses
        SET title = ?, amount = ?, date = ?, category_id = ?
      WHERE id = ? AND user_id = ?`,
      [title, amount, date, category_id ?? null, id, user_id]
    );

    if ((result as any).affectedRows === 0) return null;

    const [rows] = await pool.query(
      `SELECT e.id, e.title, e.amount, e.date,
            c.id AS category_id, c.name AS category_name, c.color AS category_color
       FROM expenses e
       LEFT JOIN categories c ON e.category_id = c.id
      WHERE e.id = ? AND e.user_id = ?`,
      [id, user_id]
    );

    const r = (rows as any[])[0];
    return {
      id: r.id,
      title: r.title,
      amount: r.amount,
      date: r.date,
      category: r.category_id
        ? { id: r.category_id, name: r.category_name, color: r.category_color }
        : null,
    };
  },

  async findByIdForUser(id: number, userId: number) {
    const [rows] = await pool.query(
      `SELECT e.id, e.user_id, e.title, e.amount, e.date, e.category_id
         FROM expenses e
        WHERE e.id = ? AND e.user_id = ?`,
      [id, userId]
    );
    return (rows as any[])[0] || null;
  },
};
