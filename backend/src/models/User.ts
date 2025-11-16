import { pool } from "../config/db.js";

export interface User {
  id?: number;
  email: string;
  password: string;
  created_at?: Date;
}

export const UserModel = {
  async findByEmail(email: string): Promise<User | null> {
    const [rows] = await pool.query("SELECT * FROM users WHERE email = ?", [email]);
    const result = (rows as User[])[0];
    return result || null;
  },

  async create(user: User): Promise<number> {
    const [result] = await pool.query(
      "INSERT INTO users (email, password) VALUES (?, ?)",
      [user.email, user.password]
    );
    return (result as any).insertId as number;
  },

  async deleteById(id: number): Promise<void> {
    await pool.query("DELETE FROM users WHERE id = ?", [id]);
  },
};
