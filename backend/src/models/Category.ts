import { pool } from "../config/db.js";

export interface Category {
  id?: number;
  user_id: number;
  name: string;
  color?: number;
  keywords?: string[];
}

export const CategoryModel = {
  async getAllByUser(userId: number) {
    const [rows] = await pool.query(
      `SELECT c.id, c.name, c.color,
              GROUP_CONCAT(k.keyword ORDER BY k.keyword SEPARATOR ',') AS keywords
         FROM categories c
         LEFT JOIN category_keywords k ON k.category_id = c.id
        WHERE c.user_id = ?
        GROUP BY c.id
        ORDER BY c.name;`,
      [userId]
    );

    // Transformar la lista en arrays
    return (rows as any[]).map((r) => ({
      id: r.id,
      name: r.name,
      color: r.color,
      keywords: r.keywords ? r.keywords.split(",") : [],
    }));
  },

  async create(category: Category) {
    const conn = await pool.getConnection();
    try {
      await conn.beginTransaction();

      const [result] = await conn.query(
        "INSERT INTO categories (user_id, name, color) VALUES (?, ?, ?)",
        [category.user_id, category.name, category.color || null]
      );
      const categoryId = (result as any).insertId;

      if (category.keywords?.length) {
        const values = category.keywords.map((k) => [categoryId, k]);
        await conn.query(
          "INSERT INTO category_keywords (category_id, keyword) VALUES ?",
          [values]
        );
      }

      await conn.commit();
      return { id: categoryId, ...category };
    } catch (err) {
      await conn.rollback();
      throw err;
    } finally {
      conn.release();
    }
  },

  async remove(id: number, userId: number) {
    const [result] = await pool.query(
      "DELETE FROM categories WHERE id = ? AND user_id = ?",
      [id, userId]
    );
    return (result as any).affectedRows > 0;
  },

  async findByName(userId: number, name: string) {
    const [rows] = await pool.query(
      `SELECT id, name, color FROM categories WHERE user_id = ? AND name = ? LIMIT 1`,
      [userId, name]
    );
    return (rows as any[])[0] ?? null;
  },
};
