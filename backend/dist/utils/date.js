export function sanitizeDateInput(value) {
    if (!value) {
        throw new Error("Fecha requerida");
    }
    const trimmed = value.trim();
    if (/^\d{4}-\d{2}-\d{2}$/.test(trimmed)) {
        return trimmed;
    }
    const parsed = new Date(trimmed);
    if (Number.isNaN(parsed.getTime())) {
        throw new Error("Fecha inválida");
    }
    return parsed.toISOString().slice(0, 10);
}
