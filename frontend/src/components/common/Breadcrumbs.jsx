import React from 'react';
import { ChevronRight, Home, Folder } from 'lucide-react';

export default function Breadcrumbs({ currentFolderStack, onNavigateToRoot, onNavigateToFolder }) {
  return (
    <nav className="flex items-center space-x-1.5 text-sm font-medium text-muted py-2 px-1">
      <button
        onClick={onNavigateToRoot}
        className="flex items-center space-x-1 hover:text-accent-blue transition-colors px-2 py-1 rounded-lg hover:bg-surface/50"
      >
        <Home className="w-4 h-4" />
        <span>Root</span>
      </button>

      {currentFolderStack.map((folder, index) => {
        const isLast = index === currentFolderStack.length - 1;
        return (
          <React.Fragment key={folder.id}>
            <ChevronRight className="w-4 h-4 text-surface-dark font-bold" />
            <button
              onClick={() => onNavigateToFolder(index)}
              disabled={isLast}
              className={`flex items-center space-x-1.5 px-2 py-1 rounded-lg transition-colors max-w-[150px] truncate ${
                isLast
                  ? 'text-charcoal font-semibold cursor-default bg-surface/40'
                  : 'hover:text-accent-blue hover:bg-surface/50'
              }`}
            >
              <Folder className="w-3.5 h-3.5 shrink-0 text-accent-blue" />
              <span className="truncate">{folder.name}</span>
            </button>
          </React.Fragment>
        );
      })}
    </nav>
  );
}
