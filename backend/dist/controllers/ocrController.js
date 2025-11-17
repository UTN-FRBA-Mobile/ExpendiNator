import { DEFAULT_CATEGORIES } from "../config/defaultCategories.js";
import { CategoryModel } from "../models/Category.js";
const CATEGORY_TITLES = {
    "Supermercado": ["Leche 1L", "Pan lactal", "Queso cremoso", "Detergente", "Fideos", "Yerba"],
    "Transporte": ["Uber viaje", "Subte", "Colectivo", "Nafta", "Peaje"],
    "Comida afuera": ["Pizza muzza", "Hamburguesa doble", "Empanadas", "Café", "Sushi combo"],
    "Salidas": ["Cine 2D", "Teatro", "Concierto", "Bar cerveza"],
    "Farmacia": ["Ibuprofeno", "Alcohol", "Jabón líquido", "Protector solar"],
};
function todayISO() {
    const d = new Date();
    const pad = (n) => String(n).padStart(2, "0");
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}
function rand(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}
function pick(arr) {
    return arr[rand(0, arr.length - 1)];
}
function fallbackTitles(categoryName) {
    return [
        `${categoryName} básico`,
        `${categoryName} especial`,
        `${categoryName} oferta`,
        `${categoryName} extra`,
    ];
}
async function buildCategoryTemplates(userId) {
    const categories = await CategoryModel.getAllByUser(userId);
    if (categories.length) {
        return categories.map((cat) => ({
            id: cat.id,
            name: cat.name,
            sampleTitles: CATEGORY_TITLES[cat.name] ?? fallbackTitles(cat.name),
        }));
    }
    return DEFAULT_CATEGORIES.map((c) => ({
        id: null,
        name: c.name,
        sampleTitles: CATEGORY_TITLES[c.name] ?? fallbackTitles(c.name),
    }));
}
function buildRandomItems(categories) {
    const usableCategories = categories.length
        ? categories
        : [{ id: null, name: "Sin categoría", sampleTitles: fallbackTitles("Producto") }];
    const date = todayISO();
    // 50% de probabilidad de varios ítems misma categoría,
    // 50% de "mixed" (varias categorías).
    const clustered = Math.random() < 0.5;
    const count = rand(1, 3);
    const items = [];
    if (clustered) {
        const cat = pick(usableCategories);
        for (let i = 0; i < count; i++) {
            const title = pick(cat.sampleTitles);
            const amount = rand(1200, 9800) + Math.round(Math.random() * 99) / 100;
            items.push({
                title,
                amount: Number(amount.toFixed(2)),
                categoryId: cat.id,
                categoryName: cat.name,
                date,
            });
        }
    }
    else {
        for (let i = 0; i < count; i++) {
            const cat = pick(usableCategories);
            const title = pick(cat.sampleTitles);
            const amount = rand(1200, 9800) + Math.round(Math.random() * 99) / 100;
            items.push({
                title,
                amount: Number(amount.toFixed(2)),
                categoryId: cat.id,
                categoryName: cat.name,
                date,
            });
        }
    }
    return items;
}
function aggregateByCategory(items) {
    const map = new Map();
    for (const it of items) {
        const key = it.categoryName || "Sin categoría";
        if (!map.has(key)) {
            map.set(key, { category: key, amount: 0, itemsCount: 0, items: [] });
        }
        const agg = map.get(key);
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
    async parse(req, res) {
        try {
            const userId = req.userId;
            const categories = await buildCategoryTemplates(userId);
            const items = buildRandomItems(categories);
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
        }
        catch (err) {
            console.error(err);
            return res.status(500).json({ error: "Mock OCR simple error" });
        }
    }
};
