import React from 'react';
import { Bell, CheckCheck, FileText, Share2, Trash2 } from 'lucide-react';
import { useNotificationStore } from '../../store/useNotificationStore';

export default function NotificationDropdown({ onClose }) {
  const { notifications, unreadCount, markAllAsRead } = useNotificationStore();

  const getNotificationIcon = (type = '') => {
    if (type.includes('UPLOAD')) return <FileText className="w-4 h-4 text-accent-blue" />;
    if (type.includes('SHARE')) return <Share2 className="w-4 h-4 text-accent-green" />;
    if (type.includes('DELETE')) return <Trash2 className="w-4 h-4 text-accent-red" />;
    return <Bell className="w-4 h-4 text-accent-yellow" />;
  };

  return (
    <div className="absolute right-0 mt-2 w-80 bg-base rounded-2xl shadow-card border border-surface py-2 z-40 animate-fadeIn">
      <div className="px-4 py-2 border-b border-surface/60 flex items-center justify-between">
        <div className="flex items-center space-x-2">
          <h4 className="font-bold text-xs text-charcoal">Kafka Notifications</h4>
          {unreadCount > 0 && (
            <span className="bg-accent-red text-white text-[10px] font-bold px-1.5 py-0.2 rounded-full">
              {unreadCount} new
            </span>
          )}
        </div>
        <button
          onClick={markAllAsRead}
          className="text-[11px] font-medium text-accent-blue hover:underline flex items-center space-x-1"
        >
          <CheckCheck className="w-3 h-3" />
          <span>Mark read</span>
        </button>
      </div>

      <div className="max-h-72 overflow-y-auto divide-y divide-surface/40">
        {notifications.length === 0 ? (
          <div className="py-8 text-center text-xs text-muted">No notifications yet.</div>
        ) : (
          notifications.map((n) => (
            <div
              key={n.id}
              className={`p-3 text-xs transition-colors flex items-start space-x-3 ${
                !n.read ? 'bg-accent-blue/5 font-medium' : 'hover:bg-surface/30 text-muted'
              }`}
            >
              <div className="p-1.5 rounded-lg bg-surface shrink-0 mt-0.5">
                {getNotificationIcon(n.type)}
              </div>
              <div className="min-w-0 flex-1">
                <p className="text-charcoal leading-snug break-words">{n.message}</p>
                <span className="text-[10px] text-muted block mt-1">
                  {new Date(n.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                </span>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
