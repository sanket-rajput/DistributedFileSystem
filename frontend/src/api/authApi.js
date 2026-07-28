import api from './client';

export const authApi = {
  login: async (email, password) => {
    const response = await api.post('/auth/login', { email, password });
    return response.data;
  },

  register: async (email, password, role = 'USER') => {
    const response = await api.post('/auth/register', { email, password, role });
    return response.data;
  },

  getMe: async () => {
    const response = await api.get('/users/me');
    return response.data;
  }
};
