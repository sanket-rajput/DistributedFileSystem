import api from './client';

export const notificationApi = {
  getNotifications: async (page = 0, size = 10) => {
    const response = await api.get('/notifications', {
      params: { page, size }
    });
    return response.data;
  }
};
