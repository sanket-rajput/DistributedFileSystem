import React, { useEffect, useState } from 'react';
import { History, X, Download, RotateCcw, FileText, CheckCircle2 } from 'lucide-react';
import { versionApi } from '../../api/versionApi';
import { formatBytes } from './FileCard';
import toast from 'react-hot-toast';

export default function VersionHistoryPanel({ file, isOpen, onClose, onRestoreSuccess }) {
  const [versions, setVersions] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (file && isOpen) {
      loadVersions();
    }
  }, [file, isOpen]);

  const loadVersions = async () => {
    setLoading(true);
    try {
      const res = await versionApi.getVersions(file.id);
      setVersions(res.data || []);
    } catch (err) {
      toast.error('Failed to load version history');
    } finally {
      setLoading(false);
    }
  };

  const handleDownload = async (versionNumber) => {
    try {
      toast.loading('Downloading historical version...', { id: 'verDownload' });
      await versionApi.downloadVersion(file.id, versionNumber, file.originalFilename);
      toast.success(`Downloaded version v${versionNumber}`, { id: 'verDownload' });
    } catch (err) {
      toast.error('Failed to download version', { id: 'verDownload' });
    }
  };

  const handleRestore = async (versionNumber) => {
    try {
      await versionApi.restoreVersion(file.id, versionNumber);
      toast.success(`Restored version v${versionNumber} as active version`);
      loadVersions();
      onRestoreSuccess();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to restore version');
    }
  };

  if (!isOpen || !file) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-hidden bg-charcoal/40 backdrop-blur-xs flex justify-end animate-fadeIn">
      <div className="w-full max-w-md bg-base h-full shadow-2xl border-l border-surface flex flex-col justify-between">
        {/* Header */}
        <div className="p-6 border-b border-surface/70 flex items-center justify-between bg-panel/40">
          <div className="flex items-center space-x-3 truncate">
            <div className="w-10 h-10 rounded-xl bg-accent-blue/10 text-accent-blue flex items-center justify-center shrink-0">
              <History className="w-5 h-5" />
            </div>
            <div className="truncate">
              <h3 className="font-bold text-base text-charcoal truncate" title={file.originalFilename}>
                {file.originalFilename}
              </h3>
              <p className="text-xs text-muted">Version History & Management</p>
            </div>
          </div>
          <button onClick={onClose} className="p-1.5 text-muted hover:text-charcoal rounded-lg">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content Body */}
        <div className="flex-1 overflow-y-auto p-6 space-y-4">
          {loading ? (
            <div className="flex flex-col items-center justify-center py-12 space-y-3">
              <div className="w-6 h-6 border-2 border-accent-blue border-t-transparent rounded-full animate-spin" />
              <p className="text-xs text-muted">Loading version history...</p>
            </div>
          ) : versions.length === 0 ? (
            <div className="text-center py-12 text-muted text-xs">No version history found.</div>
          ) : (
            versions.map((ver, idx) => {
              const isCurrent = ver.versionNumber === file.currentVersionNumber;
              return (
                <div
                  key={ver.id}
                  className={`p-4 rounded-2xl border transition-all ${
                    isCurrent
                      ? 'bg-accent-blue/5 border-accent-blue/30 shadow-soft'
                      : 'bg-base border-surface/80 hover:border-surface'
                  }`}
                >
                  <div className="flex items-start justify-between">
                    <div className="space-y-1">
                      <div className="flex items-center space-x-2">
                        <span className="font-bold text-sm text-charcoal">v{ver.versionNumber}</span>
                        {isCurrent && (
                          <span className="inline-flex items-center space-x-1 px-2 py-0.5 rounded-full text-[10px] font-bold bg-accent-blue/10 text-accent-blue">
                            <CheckCircle2 className="w-3 h-3" />
                            <span>Active</span>
                          </span>
                        )}
                      </div>
                      <p className="text-xs text-muted">
                        Uploaded on {new Date(ver.createdAt).toLocaleString()}
                      </p>
                      <p className="text-[11px] font-mono text-muted/80">
                        Size: {formatBytes(ver.sizeBytes)}
                      </p>
                    </div>

                    <div className="flex items-center space-x-1">
                      <button
                        onClick={() => handleDownload(ver.versionNumber)}
                        title="Download version"
                        className="p-2 text-muted hover:text-accent-blue hover:bg-accent-blue/10 rounded-xl transition-colors"
                      >
                        <Download className="w-4 h-4" />
                      </button>
                      {!isCurrent && (
                        <button
                          onClick={() => handleRestore(ver.versionNumber)}
                          title="Restore version"
                          className="p-2 text-muted hover:text-accent-green hover:bg-accent-green/10 rounded-xl transition-colors"
                        >
                          <RotateCcw className="w-4 h-4" />
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              );
            })
          )}
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-surface/70 bg-panel/30 text-center">
          <p className="text-[11px] text-muted">
            Restoring a version promotes it to the active file state.
          </p>
        </div>
      </div>
    </div>
  );
}
