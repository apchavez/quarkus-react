export type Product = {
  id?: string;
  sku: string;
  name: string;
  description: string;
  category: string;
  price: number;
  stock: number;
  active: boolean;
};

export type ProductsPage = {
  content: Product[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
};
