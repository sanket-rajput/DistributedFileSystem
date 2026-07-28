import React from 'react';
import FolderCard from './FolderCard';
import FileCard from './FileCard';
import { FolderOpen } from 'lucide-react';

export default function FileGrid({
  folders = [],
  files = [],
  viewMode = 'grid',
  onFolderOpen,
  onFolderRename,
  onFolderDelete,
  onFileDownload,
  onFileShare,
  onFileVersions,
  onFileDelete
}) {
  const isEmpty = folders.length === 0 && files.length === 0;

  if (isEmpty) {
    return (
      <div className="flex flex-col items-center justify-center py-20 px-4 text-center">
        <div className="w-20 h-20 rounded-3xl bg-panel/80 flex items-center justify-center mb-4 border border-surface shadow-soft">
          <FolderOpen className="w-10 h-10 text-muted" />
        </div>
        <h3 className="font-bold text-lg text-charcoal">This folder is empty</h3>
        <p className="text-xs text-muted max-w-sm mt-1">
          Upload a file or create a folder to organize your data.
        </p>
      </div>
    );
  }

  if (viewMode === 'list') {
    return (
      <div className="bg-base rounded-2xl border border-surface/70 shadow-soft overflow-hidden">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-surface/30 text-muted text-xs font-semibold uppercase tracking-wider border-b border-surface/60">
              <th className="py-3 px-4">Name</th>
              <th className="py-3 px-4">Type</th>
              <th className="py-3 px-4">Size</th>
              <th className="py-3 px-4">Date</th>
              <th className="py-3 px-4 text-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            {folders.map(folder => (
              <FolderCard
                key={folder.id}
                folder={folder}
                viewMode="list"
                onOpen={onFolderOpen}
                onRename={onFolderRename}
                onDelete={onFolderDelete}
              />
            ))}
            {files.map(file => (
              <FileCard
                key={file.id}
                file={file}
                viewMode="list"
                onDownload={onFileDownload}
                onShare={onFileShare}
                onVersions={onFileVersions}
                onDelete={onFileDelete}
              />
            ))}
          </tbody>
        </table>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {folders.length > 0 && (
        <div>
          <h3 className="text-xs font-bold text-muted uppercase tracking-wider mb-3">Folders ({folders.length})</h3>
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
            {folders.map(folder => (
              <FolderCard
                key={folder.id}
                folder={folder}
                viewMode="grid"
                onOpen={onFolderOpen}
                onRename={onFolderRename}
                onDelete={onFolderDelete}
              />
            ))}
          </div>
        </div>
      )}

      {files.length > 0 && (
        <div>
          <h3 className="text-xs font-bold text-muted uppercase tracking-wider mb-3">Files ({files.length})</h3>
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            {files.map(file => (
              <FileCard
                key={file.id}
                file={file}
                viewMode="grid"
                onDownload={onFileDownload}
                onShare={onFileShare}
                onVersions={onFileVersions}
                onDelete={onFileDelete}
              />
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
