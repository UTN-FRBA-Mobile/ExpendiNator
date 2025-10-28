import { Request, Response } from "express";

type Item = { title: string; amount: number; category: string; date: string };

const CATEGORY_TITLES: Record<string, string[]> = {
  "Supermercado": ["Leche 1L", "Pan lactal", "Queso cremoso", "Detergente", "Fideos", "Yerba"],
  "Transporte": ["Uber viaje", "Subte", "Colectivo", "Nafta", "Peaje"],
  "Comida afuera": ["Pizza muzza", "Hamburguesa doble", "Empanadas", "Café", "Sushi combo"],
  "Salidas": ["Cine 2D", "Teatro", "Concierto", "Bar cerveza"],
  "Farmacia": ["Ibuprofeno", "Alcohol", "Jabón líquido", "Protector solar"],
};

const CATEGORY_LIST = Object.keys(CATEGORY_TITLES);

function todayISO(): string {
  const d = new Date();
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

function rand(min: number, max: number) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function pick<T>(arr: T[]) {
  return arr[rand(0, arr.length - 1)];
}

function buildRandomItems(): Item[] {
  const date = todayISO();

  // 50% de probabilidad de varios ítems misma categoría,
  // 50% de "mixed" (varias categorías).
  const clustered = Math.random() < 0.5;

  const count = rand(1, 3);
  const items: Item[] = [];

  if (clustered) {
    const cat = pick(CATEGORY_LIST);
    for (let i = 0; i < count; i++) {
      const title = pick(CATEGORY_TITLES[cat]);
      const amount = rand(1200, 9800) + Math.round(Math.random() * 99) / 100;
      items.push({ title, amount: Number(amount.toFixed(2)), category: cat, date });
    }
  } else {
    for (let i = 0; i < count; i++) {
      const cat = pick(CATEGORY_LIST);
      const title = pick(CATEGORY_TITLES[cat]);
      const amount = rand(1200, 9800) + Math.round(Math.random() * 99) / 100;
      items.push({ title, amount: Number(amount.toFixed(2)), category: cat, date });
    }
  }

  return items;
}

function aggregateByCategory(items: Item[]) {
  const map = new Map<string, { category: string; amount: number; itemsCount: number; items: Array<{ title: string; amount: number }> }>();
  for (const it of items) {
    const key = it.category || "Sin categoría";
    if (!map.has(key)) {
      map.set(key, { category: key, amount: 0, itemsCount: 0, items: [] });
    }
    const agg = map.get(key)!;
    agg.amount += it.amount;
    agg.itemsCount += 1;
    agg.items.push({ title: it.title, amount: it.amount });
  }
  // ordenar de mayor a menor gasto
  return Array.from(map.values()).sort((a, b) => b.amount - a.amount).map(g => ({
    category: g.category,
    amount: Number(g.amount.toFixed(2)),
    itemsCount: g.itemsCount,
    items: g.items
  }));
}

export const MockOcrSimpleController = {
  async parse(_req: Request, res: Response) {
    try {
      const items = buildRandomItems();
      const byCategory = aggregateByCategory(items);
      const total = Number(items.reduce((acc, x) => acc + x.amount, 0).toFixed(2));

      return res.json({
        receiptId: `mock-${Math.random().toString(36).slice(2, 8)}`,
        currency: "ARS",
        date: todayISO(),
        items,
        byCategory,
        total,
      });
    } catch (err) {
      console.error(err);
      return res.status(500).json({ error: "Mock OCR simple error" });
    }
  }
};
