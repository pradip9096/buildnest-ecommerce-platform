import type { CartItem } from '../../types';

interface Props {
  item: CartItem;
  onRemove: (cartItemId: number) => void;
  onAdd: (productId: number) => void;
  removing: boolean;
}

export function CartItemRow({ item, onRemove, onAdd, removing }: Props) {
  return (
    <div className="flex items-center gap-4 py-4 border-b border-gray-100 last:border-0">
      <div className="w-16 h-16 bg-gray-100 rounded-lg flex items-center justify-center text-2xl flex-shrink-0">
        🏗️
      </div>

      <div className="flex-1 min-w-0">
        <p className="font-medium text-gray-900 truncate">{item.productName}</p>
        <p className="text-sm text-gray-500 mt-0.5">₹{item.price.toFixed(2)} each</p>
      </div>

      <div className="flex items-center gap-2">
        <button
          type="button"
          onClick={() => onAdd(item.productId)}
          className="w-7 h-7 rounded-full border border-gray-300 flex items-center justify-center text-gray-600 hover:bg-gray-50 transition-colors"
          aria-label="Increase quantity"
        >
          +
        </button>
        <span className="w-6 text-center font-medium text-sm">{item.quantity}</span>
        <button
          type="button"
          onClick={() => onRemove(item.cartItemId)}
          disabled={removing}
          className="w-7 h-7 rounded-full border border-gray-300 flex items-center justify-center text-gray-600 hover:bg-gray-50 transition-colors disabled:opacity-40"
          aria-label="Decrease quantity"
        >
          −
        </button>
      </div>

      <p className="w-24 text-right font-semibold text-gray-900">
        ₹{item.itemTotal.toFixed(2)}
      </p>

      <button
        type="button"
        onClick={() => onRemove(item.cartItemId)}
        disabled={removing}
        className="text-gray-400 hover:text-red-500 transition-colors disabled:opacity-40 p-1"
        aria-label="Remove item"
      >
        ✕
      </button>
    </div>
  );
}
