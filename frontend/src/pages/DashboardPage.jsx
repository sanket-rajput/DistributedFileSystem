import React, { useState, useEffect } from 'react';
import Header from '../components/layout/Header';
import Breadcrumbs from '../components/common/Breadcrumbs';
import FileGrid from '../components/file/FileGrid';
import CreateFolderModal from '../components/file/CreateFolderModal';
import FileUploadModal from '../components/file/FileUploadModal';
import VersionHistoryPanel from '../components/file/VersionHistoryPanel';
import ShareModal from '../components/file/ShareModal';
import ConfirmModal from '../components/common/ConfirmModal';

import { folderApi } from '../api/folderApi';
import { fileApi } from '../api/fileApi';

import { FolderPlus, UploadCloud, LayoutGrid, List } from 'lucide-react';
import toast from 'react-hot-toast';

export default function DashboardPage() {
  const [currentFolderStack, setCurrentFolderStack] = useState([]);
  const [folders, setFolders] = useState([]);
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(false);
  const [viewMode, setViewMode] = useState('grid');
  const [searchQuery, setSearchQuery] = useState('');

  // Modals
  const [showCreateFolder, setShowCreateFolder] = useState(false);
  const [showUploadFile, setShowUploadFile] = useState(false);
  const [selectedFileForShare, setSelectedFileForShare] = useState(null);
  const [selectedFileForVersions, setSelectedFileForVersions] = useState(null);

  // Deletion Confirmation
  const [deleteTarget, setDeleteTarget] = useState(null); // { type: 'file' | 'folder', data }

  const currentFolderId = currentFolderStack.length > 0
    ? currentFolderStack[currentFolderStack.length - 1].id
    : null;

  useEffect(() => {
    loadContents();
  }, [currentFolderId]);

  const loadContents = async () => {
    setLoading(true);
    try {
      const [foldersRes, filesRes] = await Promise.all([
        folderApi.getFolders(currentFolderId),
        fileApi.listFiles(currentFolderId),
      ]);
      setFolders(foldersRes.data || []);
      setFiles(filesRes.data || []);
    } catch (err) {
      toast.error('Failed to load folder contents');
    } finally {
      setLoading(false);
    }
  };

  // Breadcrumbs Navigation
  const handleNavigateToRoot = () => setCurrentFolderStack([]);
  const handleNavigateToFolderIndex = (index) => {
    setCurrentFolderStack(currentFolderStack.slice(0, index + 1));
  };

  const handleFolderOpen = (folder) => {
    setCurrentFolderStack([...currentFolderStack, folder]);
  };

  // Folder Actions
  const handleFolderRename = async (folder) => {
    const newName = prompt('Enter new folder name:', folder.name);
    if (newName && newName.trim() && newName !== folder.name) {
      try {
        await folderApi.renameFolder(folder.id, newName.trim());
        toast.success('Folder renamed');
        loadContents();
      } catch (err) {
        toast.error('Failed to rename folder');
      }
    }
  };

  // File Download
  const handleFileDownload = async (file) => {
    try {
      toast.loading('Preparing file download...', { id: 'fileDl' });
      await fileApi.downloadFile(file.id, file.originalFilename);
      toast.success('Download started!', { id: 'fileDl' });
    } catch (err) {
      toast.error('Download failed', { id: 'fileDl' });
    }
  };

  // Delete Action Execution
  const handleConfirmDelete = async () => {
    if (!deleteTarget) return;

    try {
      if (deleteTarget.type === 'folder') {
        await folderApi.deleteFolder(deleteTarget.data.id);
        toast.success(`Folder "${deleteTarget.data.name}" deleted`);
      } else {
        await fileApi.deleteFile(deleteTarget.data.id);
        toast.success(`File "${deleteTarget.data.originalFilename}" deleted`);
      }
      loadContents();
    } catch (err) {
      toast.error('Failed to delete item');
    } finally {
      setDeleteTarget(null);
    }
  };

  // Filtered Items (if searching from header)
  const filteredFolders = folders.filter(f =>
    f.name.toLowerCase().includes((searchQuery || '').toLowerCase())
  );
  const filteredFiles = files.filter(f =>
    f.originalFilename.toLowerCase().includes((searchQuery || '').toLowerCase())
  );

  return (
    <div className="space-y-6">
      <Header searchQuery={searchQuery} setSearchQuery={setSearchQuery} />

      {/* Action Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        {/* Breadcrumb Navigation */}
        <Breadcrumbs
          currentFolderStack={currentFolderStack}
          onNavigateToRoot={handleNavigateToRoot}
          onNavigateToFolder={handleNavigateToFolderIndex}
        />

        {/* Action Controls */}
        <div className="flex items-center space-x-3">
          <button
            onClick={() => setShowCreateFolder(true)}
            className="px-4 py-2 bg-surface hover:bg-surface/80 text-charcoal font-semibold text-xs rounded-xl transition-all flex items-center space-x-2 border border-surface shadow-soft"
          >
            <FolderPlus className="w-4 h-4 text-accent-blue" />
            <span>New Folder</span>
          </button>

          <button
            onClick={() => setShowUploadFile(true)}
            className="px-4 py-2 bg-accent-blue hover:bg-accent-blue/90 text-white font-semibold text-xs rounded-xl shadow-card transition-all flex items-center space-x-2"
          >
            <UploadCloud className="w-4 h-4" />
            <span>Upload File</span>
          </button>

          {/* View Toggle */}
          <div className="flex items-center p-1 bg-surface/50 rounded-xl border border-surface">
            <button
              onClick={() => setViewMode('grid')}
              className={`p-1.5 rounded-lg transition-colors ${
                viewMode === 'grid' ? 'bg-base text-accent-blue shadow-soft' : 'text-muted hover:text-charcoal'
              }`}
            >
              <LayoutGrid className="w-4 h-4" />
            </button>
            <button
              onClick={() => setViewMode('list')}
              className={`p-1.5 rounded-lg transition-colors ${
                viewMode === 'list' ? 'bg-base text-accent-blue shadow-soft' : 'text-muted hover:text-charcoal'
              }`}
            >
              <List className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

      {/* Main File Explorer Container */}
      {loading ? (
        <div className="flex flex-col items-center justify-center py-20 space-y-3">
          <div className="w-8 h-8 border-3 border-accent-blue border-t-transparent rounded-full animate-spin" />
          <p className="text-xs font-semibold text-muted">Loading items...</p>
        </div>
      ) : (
        <FileGrid
          folders={filteredFolders}
          files={filteredFiles}
          viewMode={viewMode}
          onFolderOpen={handleFolderOpen}
          onFolderRename={handleFolderRename}
          onFolderDelete={(folder) => setDeleteTarget({ type: 'folder', data: folder })}
          onFileDownload={handleFileDownload}
          onFileShare={(file) => setSelectedFileForShare(file)}
          onFileVersions={(file) => setSelectedFileForVersions(file)}
          onFileDelete={(file) => setDeleteTarget({ type: 'file', data: file })}
        />
      )}

      {/* Modals & Slide-overs */}
      <CreateFolderModal
        isOpen={showCreateFolder}
        onClose={() => setShowCreateFolder(false)}
        parentFolderId={currentFolderId}
        onCreateSuccess={loadContents}
      />

      <FileUploadModal
        isOpen={showUploadFile}
        onClose={() => setShowUploadFile(false)}
        folderId={currentFolderId}
        onUploadSuccess={loadContents}
      />

      <VersionHistoryPanel
        file={selectedFileForVersions}
        isOpen={!!selectedFileForVersions}
        onClose={() => setSelectedFileForVersions(null)}
        onRestoreSuccess={loadContents}
      />

      <ShareModal
        file={selectedFileForShare}
        isOpen={!!selectedFileForShare}
        onClose={() => setSelectedFileForShare(null)}
      />

      <ConfirmModal
        isOpen={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleConfirmDelete}
        title={`Delete ${deleteTarget?.type === 'folder' ? 'Folder' : 'File'}`}
        message={`Are you sure you want to delete "${
          deleteTarget?.type === 'folder' ? deleteTarget.data.name : deleteTarget?.data.originalFilename
        }"? This action cannot be undone.`}
      />
    </div>
  );
}
