import { OrdersTab } from '../components/seller/OrdersTab';

export function SellerDashboardPage() {
  return (
    <div className="min-h-screen bg-gray-50">
      <main className="max-w-6xl mx-auto px-4 py-8">
        <div className="mb-6 flex items-center gap-2">
          <h1 className="text-lg font-semibold text-gray-900">Seller Dashboard</h1>
          <span className="px-2 py-0.5 bg-primary-100 text-primary-700 rounded-full text-xs font-bold uppercase">Seller</span>
        </div>
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 min-h-[500px]">
          <OrdersTab />
        </div>
      </main>
    </div>
  );
}
