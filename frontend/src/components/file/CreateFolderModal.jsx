import React, { useState } from 'react';
import { FolderPlus, X } from 'lucide-react';
import { folderApi } from '../../api/folderApi';
import toast from 'react-hot-toast';

export default function CreateFolderModal({ isOpen, onClose, parentFolderId, onCreateSuccess }) {
  const [name, setName] = useState('');
  const [loading, setLoading] = useState(false);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!name.trim()) return;

    setLoading(true);
    try {
      await folderApi.createFolder(name.trim(), parentFolderId);
      toast.success(`Folder "${name}" created`);
      setName('');
      onCreateSuccess();
      onClose();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to create folder');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-charcoal/40 backdrop-blur-xs animate-fadeIn">
      <div className="bg-base rounded-2xl max-w-md w-full p-6 shadow-card border border-surface space-y-5">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-accent-blue/10 text-accent-blue flex items-center justify-center">
              <FolderPlus className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-bold text-lg text-charcoal">New Folder</h3>
              <p className="text-xs text-muted">Create a folder to organize files</p>
            </div>
          </div>
          <button onClick={onClose} className="p-1 text-muted hover:text-charcoal rounded-lg">
            <X className="w-4 h-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-charcoal mb-1">Folder Name</label>
            <input
              type="text"
              autoFocus
              placeholder="e.g. Project Specs"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full bg-surface/40 focus:bg-base text-sm px-3.5 py-2.5 rounded-xl border border-surface focus:border-accent-blue outline-none transition-all text-charcoal"
            />
          </div>

          <div className="flex items-center justify-end space-x-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-sm font-medium text-charcoal hover:bg-surface rounded-xl transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={!name.trim() || loading}
              className="px-5 py-2 text-sm font-semibold text-white bg-accent-blue hover:bg-accent-blue/90 rounded-xl shadow-card transition-colors disabled:opacity-50 flex items-center space-x-2"
            >
              {loading ? (
                <>
                  <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                  <span>Creating...</span>
                </>
              ) : (
                <span>Create Folder</span>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
