-- Crea la base (ajustá el nombre según tu .env)
CREATE DATABASE IF NOT EXISTS expendinatordb
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE expendinatordb;

-- =========================
-- Tabla: users
-- =========================
CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- =========================
-- Tabla: categories
-- =========================
CREATE TABLE IF NOT EXISTS categories (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  name VARCHAR(100) NOT NULL,
  color BIGINT NULL, -- ARGB como Long (ej: 0xFF9EC5FE)
  CONSTRAINT uq_category_user_name UNIQUE (user_id, name),
  INDEX ix_categories_user (user_id),
  CONSTRAINT fk_categories_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- =========================
-- Tabla: category_keywords
-- Una fila por palabra clave de una categoría
-- =========================
CREATE TABLE IF NOT EXISTS category_keywords (
  id INT AUTO_INCREMENT PRIMARY KEY,
  category_id INT NOT NULL,
  keyword VARCHAR(80) NOT NULL,
  CONSTRAINT uq_category_keyword UNIQUE (category_id, keyword),
  INDEX ix_ck_category (category_id),
  INDEX ix_ck_keyword (keyword),
  CONSTRAINT fk_ck_category
    FOREIGN KEY (category_id) REFERENCES categories(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- =========================
-- Tabla: expenses
-- =========================
CREATE TABLE IF NOT EXISTS expenses (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  category_id INT NULL,
  title VARCHAR(255) NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  date DATE NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_expenses_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT fk_expenses_category
    FOREIGN KEY (category_id) REFERENCES categories(id)
    ON DELETE SET NULL
    ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- =========================
-- Tabla: budgets
-- =========================
CREATE TABLE IF NOT EXISTS budgets (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  category_id INT NOT NULL,
  limit_amount DECIMAL(12,2) NOT NULL,
  period ENUM('MONTHLY','WEEKLY','YEARLY') NOT NULL DEFAULT 'MONTHLY',
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_budgets_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT fk_budgets_category
    FOREIGN KEY (category_id) REFERENCES categories(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
