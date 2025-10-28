import { pool } from "../config/db.js";

export type BudgetPeriod = "MONTHLY" | "WEEKLY" | "YEARLY";

export interface Budget {
  id?: number;
  user_id: number;
  category_id: number;
  limit_amount: number;
  period: BudgetPeriod;
  start_date: string; // "YYYY-MM-DD"
  end_date: string; // "YYYY-MM-DD"
  category?: {
    id: number;
    name: string;
    color?: number;
  };
}

export const BudgetModel = {
  async getAllByUser(userId: number) {
    const [rows] = await pool.query(
      `SELECT b.id, b.limit_amount, b.period, b.start_date, b.end_date,
              c.id AS category_id, c.name AS category_name, c.color AS category_color
         FROM budgets b
         JOIN categories c ON b.category_id = c.id
        WHERE b.user_id = ?
        ORDER BY b.start_date DESC, c.name ASC`,
      [userId]
    );

    return (rows as any[]).map((r) => ({
      id: r.id,
      limit_amount: Number(r.limit_amount),
      period: r.period as BudgetPeriod,
      start_date: r.start_date,
      end_date: r.end_date,
      category_id: r.category_id,
      user_id: userId,
      category: {
        id: r.category_id,
        name: r.category_name,
        color: r.category_color,
      },
    }));
  },

  async create(b: Budget) {
    const [result] = await pool.query(
      `INSERT INTO budgets (user_id, category_id, limit_amount, period, start_date, end_date)
       VALUES (?, ?, ?, ?, ?, ?)`,
      [
        b.user_id,
        b.category_id,
        b.limit_amount,
        b.period,
        b.start_date,
        b.end_date,
      ]
    );

    const id = (result as any).insertId;

    const [rows] = await pool.query(
      `SELECT b.id, b.limit_amount, b.period, b.start_date, b.end_date,
              c.id AS category_id, c.name AS category_name, c.color AS category_color
         FROM budgets b
         JOIN categories c ON b.category_id = c.id
        WHERE b.id = ? AND b.user_id = ?`,
      [id, b.user_id]
    );
    const r = (rows as any[])[0];

    return {
      id: r.id,
      limit_amount: Number(r.limit_amount),
      period: r.period as BudgetPeriod,
      start_date: r.start_date,
      end_date: r.end_date,
      category_id: r.category_id,
      user_id: b.user_id,
      category: {
        id: r.category_id,
        name: r.category_name,
        color: r.category_color,
      },
    } as Budget;
  },

  async findByIdForUser(id: number, userId: number) {
    const [rows] = await pool.query(
      `SELECT id, user_id, category_id, limit_amount, period, start_date, end_date
         FROM budgets
        WHERE id = ? AND user_id = ?`,
      [id, userId]
    );
    return (rows as any[])[0] || null;
  },

  async update(b: Budget) {
    const [result] = await pool.query(
      `UPDATE budgets
          SET category_id = ?, limit_amount = ?, period = ?, start_date = ?, end_date = ?
        WHERE id = ? AND user_id = ?`,
      [
        b.category_id,
        b.limit_amount,
        b.period,
        b.start_date,
        b.end_date,
        b.id,
        b.user_id,
      ]
    );
    if ((result as any).affectedRows === 0) return null;

    const [rows] = await pool.query(
      `SELECT b.id, b.limit_amount, b.period, b.start_date, b.end_date,
              c.id AS category_id, c.name AS category_name, c.color AS category_color
         FROM budgets b
         JOIN categories c ON b.category_id = c.id
        WHERE b.id = ? AND b.user_id = ?`,
      [b.id, b.user_id]
    );
    const r = (rows as any[])[0];
    return {
      id: r.id,
      limit_amount: Number(r.limit_amount),
      period: r.period as BudgetPeriod,
      start_date: r.start_date,
      end_date: r.end_date,
      category_id: r.category_id,
      user_id: b.user_id,
      category: {
        id: r.category_id,
        name: r.category_name,
        color: r.category_color,
      },
    } as Budget;
  },

  async remove(id: number, userId: number) {
    const [result] = await pool.query(
      "DELETE FROM budgets WHERE id = ? AND user_id = ?",
      [id, userId]
    );
    return (result as any).affectedRows > 0;
  },
};
