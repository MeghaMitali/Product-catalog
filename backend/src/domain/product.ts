export interface Product {
  id: number;
  title: string;
  description: string;
  price: number;
  currency: "USD";
  category: string;
  imageUrl: string;
  rating: {
    average: number;
    count: number;
  };
}
