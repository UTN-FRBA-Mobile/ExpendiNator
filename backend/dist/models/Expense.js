import { pool } from "../config/db.js";
export const ExpenseModel = {
    async getAllByUser(userId) {
        const [rows] = await pool.query(`SELECT e.id, e.title, e.amount, e.date,
              c.id AS category_id, c.name AS category_name, c.color AS category_color
         FROM expenses e
         LEFT JOIN categories c ON e.category_id = c.id
        WHERE e.user_id = ?
        ORDER BY e.date DESC`, [userId]);
        return rows.map(mapRowToDto);
    },
    async create(expense) {
        const { user_id, title, amount, date, category_id } = expense;
        const [result] = await pool.query(`INSERT INTO expenses (user_id, title, amount, date, category_id)
       VALUES (?, ?, ?, ?, ?)`, [user_id, title, amount, date, category_id || null]);
        const id = result.insertId;
        return await ExpenseModel.findDtoByIdForUser(id, user_id);
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
        return mapRowToDto(r);
    },
    async findByIdForUser(id, userId) {
        const [rows] = await pool.query(`SELECT e.id, e.user_id, e.title, e.amount, e.date, e.category_id
         FROM expenses e
        WHERE e.id = ? AND e.user_id = ?`, [id, userId]);
        return rows[0] || null;
    },
    async findDtoByIdForUser(id, userId) {
        const [rows] = await pool.query(`SELECT e.id, e.title, e.amount, e.date,
              c.id AS category_id, c.name AS category_name, c.color AS category_color
         FROM expenses e
         LEFT JOIN categories c ON e.category_id = c.id
        WHERE e.id = ? AND e.user_id = ?`, [id, userId]);
        return mapRowToDto(rows[0]);
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
function mapRowToDto(row) {
    if (!row)
        return null;
    return {
        id: row.id,
        title: row.title,
        amount: Number(row.amount),
        date: row.date,
        category_id: row.category_id ?? null,
        category_name: row.category_name ?? null,
        category_color: row.category_color ?? null,
    };
}
