import React, { useState, useEffect } from 'react';
import { Search, Bell, X } from 'lucide-react';
import { useNotificationStore } from '../../store/useNotificationStore';
import NotificationDropdown from '../notification/NotificationDropdown';

export default function Header({ searchQuery, setSearchQuery }) {
  const [showNotifications, setShowNotifications] = useState(false);
  const { unreadCount, fetchNotifications } = useNotificationStore();

  useEffect(() => {
    fetchNotifications();
    const interval = setInterval(fetchNotifications, 15000); // poll every 15s
    return () => clearInterval(interval);
  }, []);

  return (
    <header className="sticky top-0 z-10 bg-base/80 backdrop-blur-md border-b border-surface/50 px-8 py-3.5 flex items-center justify-between shadow-soft">
      {/* Top Gemini Gradient Line */}
      <div className="absolute top-0 left-0 right-0 h-[2px] gemini-gradient-bar" />

      {/* Search Input */}
      <div className="relative w-96">
        <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-muted" />
        <input
          type="text"
          placeholder="Search files and folders..."
          value={searchQuery || ''}
          onChange={(e) => setSearchQuery && setSearchQuery(e.target.value)}
          className="w-full bg-surface/40 hover:bg-surface/70 focus:bg-base text-sm pl-10 pr-9 py-2 rounded-xl border border-transparent focus:border-accent-blue/40 outline-none transition-all duration-200 text-charcoal placeholder:text-muted"
        />
        {searchQuery && (
          <button
            onClick={() => setSearchQuery('')}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-muted hover:text-charcoal"
          >
            <X className="w-3.5 h-3.5" />
          </button>
        )}
      </div>

      {/* Right Controls */}
      <div className="flex items-center space-x-4">
        {/* Notification Bell */}
        <div className="relative">
          <button
            onClick={() => setShowNotifications(!showNotifications)}
            className="relative p-2.5 rounded-xl bg-surface/40 hover:bg-surface text-charcoal transition-colors border border-transparent hover:border-surface"
            title="Notifications"
          >
            <Bell className="w-4 h-4" />
            {unreadCount > 0 && (
              <span className="absolute -top-1 -right-1 w-5 h-5 bg-accent-red text-white text-[10px] font-bold rounded-full flex items-center justify-center shadow-sm">
                {unreadCount > 9 ? '9+' : unreadCount}
              </span>
            )}
          </button>

          {showNotifications && (
            <NotificationDropdown onClose={() => setShowNotifications(false)} />
          )}
        </div>
      </div>
    </header>
  );
}
