import api from './client';

export const versionApi = {
  getVersions: async (fileId) => {
    const response = await api.get(`/files/${fileId}/versions`);
    return response.data;
  },

  downloadVersion: async (fileId, versionNumber, filename) => {
    const response = await api.get(`/files/${fileId}/versions/${versionNumber}/download`, {
      responseType: 'blob',
    });

    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `v${versionNumber}_${filename}`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },

  restoreVersion: async (fileId, versionNumber) => {
    const response = await api.post(`/files/${fileId}/versions/${versionNumber}/restore`);
    return response.data;
  }
};
