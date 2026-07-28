import api from './client';

const baseURL = import.meta.env.VITE_API_BASE_URL || '/api/v1';

export const shareApi = {
  createShare: async (fileId, expiresAt = null, permission = 'DOWNLOAD') => {
    const response = await api.post(`/files/${fileId}/share`, { expiresAt, permission });
    return response.data;
  },

  getShareForFile: async (fileId) => {
    const response = await api.get(`/files/${fileId}/share`);
    return response.data;
  },

  getShareByToken: async (token) => {
    const response = await api.get(`/share/${token}`);
    return response.data;
  },

  getPublicStreamUrl: (token, inline = true) => {
    return `${baseURL}/share/${token}/stream?inline=${inline}`;
  },

  downloadSharedFile: async (token, filename) => {
    const streamUrl = `${baseURL}/share/${token}/stream?inline=false`;
    const response = await api.get(`/share/${token}/stream?inline=false`, {
      responseType: 'blob',
    });

    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', filename || 'shared_file');
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },

  revokeShare: async (fileId, shareId) => {
    const response = await api.delete(`/files/${fileId}/share/${shareId}`);
    return response.data;
  }
};
