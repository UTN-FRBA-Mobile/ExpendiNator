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

export interface BudgetUsageRow {
  id: number;
  user_id: number;
  category_id: number;
  limit_amount: number;
  period: BudgetPeriod;
  start_date: string; // YYYY-MM-DD
  end_date: string; // YYYY-MM-DD
  category_name: string;
  category_color: number | null;
  spent: number; // suma de expenses en el periodo,
  eff_start_date: string; // effective window (override con from/to si vienen)
  eff_end_date: string;
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

  async getUsageByUser(
    userId: number,
    opts?: { activeOnly?: boolean; from?: string | null; to?: string | null }
  ): Promise<BudgetUsageRow[]> {
    const activeOnly = !!opts?.activeOnly;
    const from = opts?.from ?? null; // YYYY-MM-DD o null
    const to = opts?.to ?? null;

    const whereActive = activeOnly
      ? "AND CURDATE() BETWEEN COALESCE(?, b.start_date) AND COALESCE(?, b.end_date)"
      : "";

    const sql = `
      SELECT
        b.id,
        b.user_id,
        b.category_id,
        b.limit_amount,
        b.period,
        DATE_FORMAT(b.start_date, '%Y-%m-%d') AS start_date,
        DATE_FORMAT(b.end_date,   '%Y-%m-%d') AS end_date,
        DATE_FORMAT(COALESCE(?, b.start_date), '%Y-%m-%d') AS eff_start_date,
        DATE_FORMAT(COALESCE(?, b.end_date),   '%Y-%m-%d') AS eff_end_date,
        c.name  AS category_name,
        c.color AS category_color,
        COALESCE(SUM(e.amount), 0) AS spent
      FROM budgets b
      JOIN categories c
        ON c.id = b.category_id
      LEFT JOIN expenses e
        ON e.user_id = b.user_id
       AND e.category_id = b.category_id
       AND e.date BETWEEN COALESCE(?, b.start_date) AND COALESCE(?, b.end_date)
      WHERE b.user_id = ?
      ${whereActive}
      GROUP BY b.id
      ORDER BY b.start_date DESC, c.name ASC
    `;

    const params: any[] = [from, to, from, to, userId];
    if (activeOnly) params.push(from, to);

    const [rows] = await pool.query(sql, params);

    return (rows as any[]).map((r) => ({
      id: r.id,
      user_id: r.user_id,
      category_id: r.category_id,
      limit_amount: Number(r.limit_amount),
      period: r.period as BudgetPeriod,
      start_date: r.start_date,
      end_date: r.end_date,
      eff_start_date: r.eff_start_date,
      eff_end_date: r.eff_end_date,
      category_name: r.category_name,
      category_color: r.category_color ?? null,
      spent: Number(r.spent),
    }));
  },
};
