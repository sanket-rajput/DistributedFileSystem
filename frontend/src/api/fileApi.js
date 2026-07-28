import api from './client';

export const fileApi = {
  uploadFile: async (file, folderId = null, onUploadProgress) => {
    const formData = new FormData();
    formData.append('file', file);

    const url = folderId ? `/files/upload?folderId=${folderId}` : '/files/upload';

    const response = await api.post(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent) => {
        if (onUploadProgress && progressEvent.total) {
          const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          onUploadProgress(percentCompleted);
        }
      },
    });
    return response.data;
  },

  getFileMetadata: async (fileId) => {
    const response = await api.get(`/files/${fileId}`);
    return response.data;
  },

  downloadFile: async (fileId, filename) => {
    const response = await api.get(`/files/${fileId}/download`, {
      responseType: 'blob',
    });

    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', filename || 'downloaded_file');
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },

  listFiles: async (folderId = null) => {
    const params = folderId ? { folderId } : {};
    const response = await api.get('/files', { params });
    return response.data;
  },

  searchFiles: async (searchParams) => {
    const response = await api.get('/files/search', { params: searchParams });
    return response.data;
  },

  deleteFile: async (fileId) => {
    const response = await api.delete(`/files/${fileId}`);
    return response.data;
  }
};
