export interface DefaultCategorySeed {
  name: string;
  color: number;
  keywords: string[];
}

export const DEFAULT_CATEGORIES: DefaultCategorySeed[] = [
  {
    name: "Supermercado",
    color: 0xff9ec5fe,
    keywords: ["super", "mercado", "almacen", "kiosco"],
  },
  {
    name: "Transporte",
    color: 0xfff3b0c3,
    keywords: ["taxi", "colectivo", "subte", "combustible", "nafta"],
  },
  {
    name: "Comida afuera",
    color: 0xfffecba1,
    keywords: ["restaurante", "delivery", "bar", "cena", "almuerzo"],
  },
  {
    name: "Salidas",
    color: 0xffe2c6fe,
    keywords: ["cine", "teatro", "show", "concierto", "ocio"],
  },
  {
    name: "Farmacia",
    color: 0xffa3cfbb,
    keywords: ["farmacia", "medicamento", "remedio", "perfumeria"],
  },
  {
    name: "Servicios",
    color: 0xffffd966,
    keywords: ["luz", "gas", "internet", "servicio"],
  },
];
