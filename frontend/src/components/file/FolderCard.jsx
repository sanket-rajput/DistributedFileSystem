import React, { useState } from 'react';
import { Folder, MoreVertical, Edit2, Trash2 } from 'lucide-react';

export default function FolderCard({ folder, onOpen, onRename, onDelete, viewMode = 'grid' }) {
  const [showMenu, setShowMenu] = useState(false);

  if (viewMode === 'list') {
    return (
      <tr className="hover:bg-surface/30 transition-colors group cursor-pointer border-b border-surface/40" onDoubleClick={() => onOpen(folder)}>
        <td className="py-3 px-4 flex items-center space-x-3">
          <div className="w-8 h-8 rounded-lg bg-accent-blue/10 text-accent-blue flex items-center justify-center">
            <Folder className="w-4 h-4" />
          </div>
          <span className="font-semibold text-sm text-charcoal truncate">{folder.name}</span>
        </td>
        <td className="py-3 px-4 text-xs text-muted">Folder</td>
        <td className="py-3 px-4 text-xs text-muted">—</td>
        <td className="py-3 px-4 text-xs text-muted">{new Date(folder.createdAt).toLocaleDateString()}</td>
        <td className="py-3 px-4 text-right relative">
          <div className="inline-block text-left">
            <button
              onClick={(e) => { e.stopPropagation(); setShowMenu(!showMenu); }}
              className="p-1 text-muted hover:text-charcoal rounded-lg opacity-0 group-hover:opacity-100 transition-opacity"
            >
              <MoreVertical className="w-4 h-4" />
            </button>
            {showMenu && (
              <div className="absolute right-0 mt-1 w-36 bg-base rounded-xl shadow-card border border-surface py-1 z-30 text-left">
                <button
                  onClick={(e) => { e.stopPropagation(); setShowMenu(false); onRename(folder); }}
                  className="w-full px-3 py-1.5 text-xs text-charcoal hover:bg-surface flex items-center space-x-2"
                >
                  <Edit2 className="w-3.5 h-3.5 text-muted" />
                  <span>Rename</span>
                </button>
                <button
                  onClick={(e) => { e.stopPropagation(); setShowMenu(false); onDelete(folder); }}
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
    <div
      onDoubleClick={() => onOpen(folder)}
      className="group relative bg-panel/60 hover:bg-panel p-4 rounded-2xl border border-surface/70 hover:border-accent-blue/30 shadow-soft hover:shadow-card transition-all duration-200 cursor-pointer flex flex-col justify-between"
    >
      <div className="flex items-start justify-between">
        <div className="w-10 h-10 rounded-xl bg-accent-blue/10 text-accent-blue flex items-center justify-center">
          <Folder className="w-5 h-5" />
        </div>
        <div className="relative">
          <button
            onClick={(e) => { e.stopPropagation(); setShowMenu(!showMenu); }}
            className="p-1 text-muted hover:text-charcoal rounded-lg opacity-0 group-hover:opacity-100 transition-opacity"
          >
            <MoreVertical className="w-4 h-4" />
          </button>
          {showMenu && (
            <div className="absolute right-0 mt-1 w-36 bg-base rounded-xl shadow-card border border-surface py-1 z-30">
              <button
                onClick={(e) => { e.stopPropagation(); setShowMenu(false); onRename(folder); }}
                className="w-full px-3 py-1.5 text-xs text-charcoal hover:bg-surface flex items-center space-x-2"
              >
                <Edit2 className="w-3.5 h-3.5 text-muted" />
                <span>Rename</span>
              </button>
              <button
                onClick={(e) => { e.stopPropagation(); setShowMenu(false); onDelete(folder); }}
                className="w-full px-3 py-1.5 text-xs text-accent-red hover:bg-accent-red/10 flex items-center space-x-2"
              >
                <Trash2 className="w-3.5 h-3.5" />
                <span>Delete</span>
              </button>
            </div>
          )}
        </div>
      </div>

      <div className="mt-4">
        <h4 className="font-semibold text-sm text-charcoal truncate" title={folder.name}>
          {folder.name}
        </h4>
        <p className="text-[11px] text-muted mt-0.5">Folder</p>
      </div>
    </div>
  );
}
