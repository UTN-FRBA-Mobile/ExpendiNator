import { pool } from "../config/db.js";
export const UserModel = {
    async findByEmail(email) {
        const [rows] = await pool.query("SELECT * FROM users WHERE email = ?", [email]);
        const result = rows[0];
        return result || null;
    },
    async create(user) {
        const [result] = await pool.query("INSERT INTO users (email, password) VALUES (?, ?)", [user.email, user.password]);
        return result.insertId;
    },
    async deleteById(id) {
        await pool.query("DELETE FROM users WHERE id = ?", [id]);
    },
};
