import { pool } from "../config/db.js";
export const ExpenseModel = {
    async getAllByUser(userId) {
        const [rows] = await pool.query(`SELECT e.id, e.title, e.amount, e.date,
              c.id AS category_id, c.name AS category_name, c.color AS category_color
         FROM expenses e
         LEFT JOIN categories c ON e.category_id = c.id
        WHERE e.user_id = ?
        ORDER BY e.date DESC`, [userId]);
        return rows.map((r) => ({
            id: r.id,
            title: r.title,
            amount: r.amount,
            date: r.date,
            category: r.category_id
                ? { id: r.category_id, name: r.category_name, color: r.category_color }
                : null,
        }));
    },
    async create(expense) {
        const { user_id, title, amount, date, category_id } = expense;
        const [result] = await pool.query(`INSERT INTO expenses (user_id, title, amount, date, category_id)
       VALUES (?, ?, ?, ?, ?)`, [user_id, title, amount, date, category_id || null]);
        return { id: result.insertId, ...expense };
    },
    async remove(id, userId) {
        const [result] = await pool.query("DELETE FROM expenses WHERE id = ? AND user_id = ?", [id, userId]);
        return result.affectedRows > 0;
    },
    async update(expense) {
        const { id, user_id, title, amount, date, category_id } = expense;
        const [result] = await pool.query(`UPDATE expenses
        SET title = ?, amount = ?, date = ?, category_id = ?
      WHERE id = ? AND user_id = ?`, [title, amount, date, category_id ?? null, id, user_id]);
        if (result.affectedRows === 0)
            return null;
        const [rows] = await pool.query(`SELECT e.id, e.title, e.amount, e.date,
            c.id AS category_id, c.name AS category_name, c.color AS category_color
       FROM expenses e
       LEFT JOIN categories c ON e.category_id = c.id
      WHERE e.id = ? AND e.user_id = ?`, [id, user_id]);
        const r = rows[0];
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
    async findByIdForUser(id, userId) {
        const [rows] = await pool.query(`SELECT e.id, e.user_id, e.title, e.amount, e.date, e.category_id
         FROM expenses e
        WHERE e.id = ? AND e.user_id = ?`, [id, userId]);
        return rows[0] || null;
    },
    async getSummaryByUser(userId, from, // YYYY-MM-DD
    to // YYYY-MM-DD
    ) {
        const sql = `
      SELECT
        e.category_id,
        COALESCE(c.name, 'Sin categoría')      AS category_name,
        c.color                                 AS category_color,
        SUM(e.amount)                           AS amount
      FROM expenses e
      LEFT JOIN categories c ON c.id = e.category_id
      WHERE e.user_id = ?
        AND (? IS NULL OR e.date >= ?)
        AND (? IS NULL OR e.date <= ?)
      GROUP BY e.category_id
      ORDER BY amount DESC;
    `;
        const params = [userId, from ?? null, from ?? null, to ?? null, to ?? null];
        const [rows] = await pool.query(sql, params);
        return rows.map((r) => ({
            category_id: r.category_id ?? null,
            category_name: r.category_name,
            category_color: r.category_color ?? null,
            amount: Number(r.amount),
        }));
    },
};
