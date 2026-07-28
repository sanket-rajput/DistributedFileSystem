import api from './client';

export const shareApi = {
  createShare: async (fileId, expiresAt = null, permission = 'DOWNLOAD') => {
    const response = await api.post(`/files/${fileId}/share`, { expiresAt, permission });
    return response.data;
  },

  getShareByToken: async (token) => {
    const response = await api.get(`/share/${token}`);
    return response.data;
  },

  downloadSharedFile: async (token, filename) => {
    const response = await api.get(`/share/${token}`, {
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
