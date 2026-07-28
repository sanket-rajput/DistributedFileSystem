import React, { useState } from 'react';
import { FileText, Image, Video, Music, Archive, File, MoreVertical, Download, Share2, History, Trash2, CopyCheck } from 'lucide-react';
import Badge from '../common/Badge';

export function formatBytes(bytes, decimals = 1) {
  if (!bytes || bytes === 0) return '0 B';
  const k = 1024;
  const dm = decimals < 0 ? 0 : decimals;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
}

export function getFileIcon(contentType = '', filename = '') {
  const type = contentType.toLowerCase();
  const name = filename.toLowerCase();

  if (type.includes('image') || name.match(/\.(jpg|jpeg|png|gif|svg|webp)$/)) {
    return <Image className="w-5 h-5 text-accent-blue" />;
  }
  if (type.includes('video') || name.match(/\.(mp4|mkv|avi|mov)$/)) {
    return <Video className="w-5 h-5 text-accent-red" />;
  }
  if (type.includes('audio') || name.match(/\.(mp3|wav|ogg)$/)) {
    return <Music className="w-5 h-5 text-accent-yellow" />;
  }
  if (type.includes('zip') || type.includes('rar') || name.match(/\.(zip|tar|gz|rar|7z)$/)) {
    return <Archive className="w-5 h-5 text-accent-green" />;
  }
  if (type.includes('pdf') || type.includes('document') || name.match(/\.(pdf|doc|docx|txt)$/)) {
    return <FileText className="w-5 h-5 text-accent-blue" />;
  }
  return <File className="w-5 h-5 text-muted" />;
}

export default function FileCard({ file, onDownload, onShare, onVersions, onDelete, viewMode = 'grid' }) {
  const [showMenu, setShowMenu] = useState(false);

  if (viewMode === 'list') {
    return (
      <tr className="hover:bg-surface/30 transition-colors group border-b border-surface/40">
        <td className="py-3 px-4 flex items-center space-x-3">
          <div className="w-8 h-8 rounded-lg bg-surface flex items-center justify-center">
            {getFileIcon(file.contentType, file.originalFilename)}
          </div>
          <div className="min-w-0">
            <span className="font-semibold text-sm text-charcoal truncate block" title={file.originalFilename}>
              {file.originalFilename}
            </span>
            {file.isDeduplicated && <Badge type="duplicate" />}
          </div>
        </td>
        <td className="py-3 px-4 text-xs text-muted truncate">{file.contentType}</td>
        <td className="py-3 px-4 text-xs text-muted">{formatBytes(file.sizeBytes)}</td>
        <td className="py-3 px-4 text-xs text-muted">{new Date(file.createdAt).toLocaleDateString()}</td>
        <td className="py-3 px-4 text-right relative">
          <div className="inline-block text-left">
            <button
              onClick={() => setShowMenu(!showMenu)}
              className="p-1 text-muted hover:text-charcoal rounded-lg opacity-0 group-hover:opacity-100 transition-opacity"
            >
              <MoreVertical className="w-4 h-4" />
            </button>
            {showMenu && (
              <div className="absolute right-0 mt-1 w-44 bg-base rounded-xl shadow-card border border-surface py-1 z-30 text-left">
                <button
                  onClick={() => { setShowMenu(false); onDownload(file); }}
                  className="w-full px-3 py-1.5 text-xs text-charcoal hover:bg-surface flex items-center space-x-2"
                >
                  <Download className="w-3.5 h-3.5 text-accent-blue" />
                  <span>Download</span>
                </button>
                <button
                  onClick={() => { setShowMenu(false); onShare(file); }}
                  className="w-full px-3 py-1.5 text-xs text-charcoal hover:bg-surface flex items-center space-x-2"
                >
                  <Share2 className="w-3.5 h-3.5 text-accent-green" />
                  <span>Share Link</span>
                </button>
                <button
                  onClick={() => { setShowMenu(false); onVersions(file); }}
                  className="w-full px-3 py-1.5 text-xs text-charcoal hover:bg-surface flex items-center space-x-2"
                >
                  <History className="w-3.5 h-3.5 text-muted" />
                  <span>Version History</span>
                </button>
                <div className="h-px bg-surface/60 my-1" />
                <button
                  onClick={() => { setShowMenu(false); onDelete(file); }}
                  className="w-full px-3 py-1.5 text-xs text-accent-red hover:bg-accent-red/10 flex items-center space-x-2"
                >
                  <Trash2 className="w-3.5 h-3.5" />
                  <span>Delete</span>
                </button>
              </div>
            )}
          </div>
        </td>
      </tr>
    );
  }

  return (
    <div className="group relative bg-base hover:bg-surface/30 p-4 rounded-2xl border border-surface shadow-soft hover:shadow-card transition-all duration-200 flex flex-col justify-between">
      <div className="flex items-start justify-between">
        <div className="w-10 h-10 rounded-xl bg-surface/60 flex items-center justify-center">
          {getFileIcon(file.contentType, file.originalFilename)}
        </div>
        <div className="relative">
          <button
            onClick={() => setShowMenu(!showMenu)}
            className="p-1 text-muted hover:text-charcoal rounded-lg opacity-0 group-hover:opacity-100 transition-opacity"
          >
            <MoreVertical className="w-4 h-4" />
          </button>
          {showMenu && (
            <div className="absolute right-0 mt-1 w-44 bg-base rounded-xl shadow-card border border-surface py-1 z-30">
              <button
                onClick={() => { setShowMenu(false); onDownload(file); }}
                className="w-full px-3 py-1.5 text-xs text-charcoal hover:bg-surface flex items-center space-x-2"
              >
                <Download className="w-3.5 h-3.5 text-accent-blue" />
                <span>Download</span>
              </button>
              <button
                onClick={() => { setShowMenu(false); onShare(file); }}
                className="w-full px-3 py-1.5 text-xs text-charcoal hover:bg-surface flex items-center space-x-2"
              >
                <Share2 className="w-3.5 h-3.5 text-accent-green" />
                <span>Share Link</span>
              </button>
              <button
                onClick={() => { setShowMenu(false); onVersions(file); }}
                className="w-full px-3 py-1.5 text-xs text-charcoal hover:bg-surface flex items-center space-x-2"
              >
                <History className="w-3.5 h-3.5 text-muted" />
                <span>Version History</span>
              </button>
              <div className="h-px bg-surface/60 my-1" />
              <button
                onClick={() => { setShowMenu(false); onDelete(file); }}
                className="w-full px-3 py-1.5 text-xs text-accent-red hover:bg-accent-red/10 flex items-center space-x-2"
              >
                <Trash2 className="w-3.5 h-3.5" />
                <span>Delete</span>
              </button>
            </div>
          )}
        </div>
      </div>

      <div className="mt-4 space-y-2">
        <h4 className="font-semibold text-sm text-charcoal truncate" title={file.originalFilename}>
          {file.originalFilename}
        </h4>
        <div className="flex items-center justify-between text-[11px] text-muted">
          <span>{formatBytes(file.sizeBytes)}</span>
          {file.isDeduplicated ? (
            <Badge type="duplicate" />
          ) : (
            <span>v{file.currentVersionNumber || 1}</span>
          )}
        </div>
      </div>
    </div>
  );
}
