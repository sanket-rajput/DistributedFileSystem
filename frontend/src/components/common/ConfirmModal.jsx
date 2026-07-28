import React from 'react';
import { AlertTriangle, X } from 'lucide-react';

export default function ConfirmModal({ isOpen, onClose, onConfirm, title, message, confirmText = 'Delete', isDanger = true }) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-charcoal/40 backdrop-blur-xs animate-fadeIn">
      <div className="bg-base rounded-2xl max-w-md w-full p-6 shadow-card border border-surface space-y-5">
        <div className="flex items-start justify-between">
          <div className="flex items-center space-x-3">
            <div className={`w-10 h-10 rounded-xl flex items-center justify-center ${isDanger ? 'bg-accent-red/10 text-accent-red' : 'bg-accent-blue/10 text-accent-blue'}`}>
              <AlertTriangle className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-bold text-lg text-charcoal">{title}</h3>
              <p className="text-xs text-muted">Confirm action</p>
            </div>
          </div>
          <button onClick={onClose} className="p-1 text-muted hover:text-charcoal rounded-lg">
            <X className="w-4 h-4" />
          </button>
        </div>

        <p className="text-sm text-charcoal/80 leading-relaxed">{message}</p>

        <div className="flex items-center justify-end space-x-3 pt-2">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-sm font-medium text-charcoal hover:bg-surface rounded-xl transition-colors"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={() => {
              onConfirm();
              onClose();
            }}
            className={`px-4 py-2 text-sm font-semibold text-white rounded-xl shadow-sm transition-colors ${
              isDanger ? 'bg-accent-red hover:bg-accent-red/90' : 'bg-accent-blue hover:bg-accent-blue/90'
            }`}
          >
            {confirmText}
          </button>
        </div>
      </div>
    </div>
  );
}
