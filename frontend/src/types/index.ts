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

export interface ReviewUser {
  id: number;
  username?: string;
  firstName?: string;
  lastName?: string;
}

export interface Review {
  id: number;
  rating: number;
  comment: string;
  helpfulCount: number;
  user?: ReviewUser;
  createdAt: string;
}

export interface ReviewSummary {
  averageRating: number;
  totalReviews: number;
  ratingDistribution: Record<string, number>;
}

export interface PagedResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
}

export interface CartItem {
  cartItemId: number;
  productId: number;
  productName: string;
  quantity: number;
  price: number;
  itemTotal: number;
}

export interface Cart {
  cartId: number;
  userId: number;
  items: CartItem[];
  totalAmount: number;
}

export interface ShippingOption {
  id: number;
  name: string;
  description?: string;
  baseCost: number;
  calculatedCost: number;
  estimatedDaysMin: number;
  estimatedDaysMax: number;
}

export type CheckoutStep = 'PENDING_SHIPPING' | 'PENDING_PAYMENT' | 'PENDING_CONFIRM';

export interface CheckoutSession {
  userId: number;
  cartId: number;
  step: CheckoutStep;
  addressId?: number;
  shippingMethodId?: number;
  shippingCost?: number;
  orderId?: number;
  razorpayOrderId?: string;
}

export interface OrderItem {
  productId: number;
  productName: string;
  quantity: number;
  price: number;
  itemTotal: number;
}

export interface Order {
  id: number;
  userId: number;
  status: string;
  totalAmount: number;
  orderItems?: OrderItem[];
  createdAt: string;
}

export interface Address {
  id: number;
  streetAddress: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  isDefault: boolean;
  addressType?: string;
}

export interface UserProfile {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  address?: string;
  roles?: string[];
}

/**
 * Login/refresh response body. Tokens travel as httpOnly cookies (SEC-15),
 * never in this JSON body.
 */
export interface AuthUserResponse {
  userId: number;
  username: string;
}

export interface AuthUser {
  id: number;
  username: string;
  roles: string[];
}
