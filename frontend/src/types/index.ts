export interface Category {
  id: number;
  name: string;
  description?: string;
}

export interface Product {
  id: number;
  name: string;
  description?: string;
  price: number;
  discountPrice?: number;
  stockQuantity: number;
  sku: string;
  category?: Category;
  imageUrl?: string;
  isActive: boolean;
  createdAt: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export type SortOption = 'relevance' | 'price_asc' | 'price_desc' | 'newest';

export interface ProductFilters {
  keyword: string;
  categoryIds: number[];
  sort: SortOption;
  page: number;
  pageSize: number;
}
