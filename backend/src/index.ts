import cors from "cors";
import dotenv from "dotenv";
import express from "express";
import { authRouter } from "./routes/authRoutes.js";
import { budgetRouter } from "./routes/budgetRoutes.js";
import { categoryRouter } from "./routes/categoryRoutes.js";
import { expenseRouter } from "./routes/expenseRoutes.js";
import { ocrRouter } from "./routes/ocrRoutes.js";

dotenv.config();
const app = express();

app.use(cors());
app.use(express.json());

app.use((req, _res, next) => {
  const now = new Date().toISOString();
  console.log(`[${now}] ${req.method} ${req.url}`);
  next();
});

app.get("/ping", (req, res) => {
  res.json({ message: "pong", time: new Date().toISOString() });
});

app.use("/auth", authRouter);
app.use("/categories", categoryRouter);
app.use("/expenses", expenseRouter);
app.use("/budgets", budgetRouter);
app.use("/ocr", ocrRouter);

const PORT = process.env.PORT || 3000;
app.listen(PORT, () =>
  console.log(`✅ Server running on http://localhost:${PORT}`)
);
