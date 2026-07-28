import { create } from 'zustand';
import { notificationApi } from '../api/notificationApi';

export const useNotificationStore = create((set, get) => ({
  notifications: [],
  unreadCount: 0,
  loading: false,

  fetchNotifications: async () => {
    set({ loading: true });
    try {
      const res = await notificationApi.getNotifications(0, 20);
      const items = res.data?.content || [];
      const unread = items.filter(n => !n.read).length;
      set({ notifications: items, unreadCount: unread, loading: false });
    } catch (err) {
      console.warn('Failed to fetch notifications:', err);
      set({ loading: false });
    }
  },

  markAllAsRead: () => {
    const updated = get().notifications.map(n => ({ ...n, read: true }));
    set({ notifications: updated, unreadCount: 0 });
  }
}));
