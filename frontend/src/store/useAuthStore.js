import { create } from 'zustand';
import { authApi } from '../api/authApi';

export const useAuthStore = create((set, get) => ({
  token: localStorage.getItem('token') || null,
  user: JSON.parse(localStorage.getItem('user') || 'null'),
  isAuthenticated: !!localStorage.getItem('token'),
  loading: false,
  error: null,

  login: async (email, password) => {
    set({ loading: true, error: null });
    try {
      const data = await authApi.login(email, password);
      const authData = data.data; // ApiResponse<AuthResponse>
      const token = authData.accessToken;
      const user = authData.user;

      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(user));

      set({ token, user, isAuthenticated: true, loading: false });
      return user;
    } catch (err) {
      const message = err.response?.data?.message || 'Login failed. Please check credentials.';
      set({ error: message, loading: false });
      throw new Error(message);
    }
  },

  register: async (email, password, role = 'USER') => {
    set({ loading: true, error: null });
    try {
      const data = await authApi.register(email, password, role);
      const authData = data.data;
      const token = authData.accessToken;
      const user = authData.user;

      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(user));

      set({ token, user, isAuthenticated: true, loading: false });
      return user;
    } catch (err) {
      const message = err.response?.data?.message || 'Registration failed. Try again.';
      set({ error: message, loading: false });
      throw new Error(message);
    }
  },

  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    set({ token: null, user: null, isAuthenticated: false });
  },

  isAdmin: () => {
    const user = get().user;
    return user?.role === 'ADMIN' || user?.role === 'ROLE_ADMIN';
  }
}));
