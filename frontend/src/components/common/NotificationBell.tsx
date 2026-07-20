import { useState, useRef, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useNotifications } from '../../hooks/useNotifications';

function formatStatus(status: string): string {
  return status
    .toLowerCase()
    .split('_')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
}

export function NotificationBell() {
  const { notifications, unreadCount, markAllRead } = useNotifications(true);
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) {
        setOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  function handleToggle() {
    setOpen(o => !o);
    if (!open) markAllRead();
  }

  return (
    <div className="relative" ref={ref}>
      <button
        type="button"
        onClick={handleToggle}
        className="relative text-gray-600 hover:text-gray-900"
        aria-label="Notifications"
      >
        <span className="text-xl">🔔</span>
        {unreadCount > 0 && (
          <span className="absolute -top-2 -right-2 bg-primary-600 text-white text-[10px] font-bold rounded-full min-w-[18px] h-[18px] flex items-center justify-center px-1">
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        )}
      </button>
      {open && (
        <div className="absolute right-0 mt-2 w-72 bg-white border border-gray-200 rounded-xl shadow-lg py-1 text-sm max-h-96 overflow-y-auto">
          {notifications.length === 0 ? (
            <p className="px-4 py-3 text-gray-500">No notifications yet</p>
          ) : (
            notifications.map(n => (
              <Link
                key={n.id}
                to={`/orders/${n.orderId}`}
                onClick={() => setOpen(false)}
                className="block px-4 py-2 text-gray-700 hover:bg-gray-50 border-b border-gray-100 last:border-b-0"
              >
                <span className="font-medium">Order #{n.orderId}</span>
                <span className="block text-gray-500 text-xs">
                  {formatStatus(n.previousStatus)} → {formatStatus(n.newStatus)}
                </span>
              </Link>
            ))
          )}
        </div>
      )}
    </div>
  );
}
