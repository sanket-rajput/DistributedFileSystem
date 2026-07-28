import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { shareApi } from '../api/shareApi';
import { getFileIcon, formatBytes } from '../components/file/FileCard';
import { Sparkles, Download, ShieldAlert, CheckCircle } from 'lucide-react';
import toast from 'react-hot-toast';

export default function PublicSharePage() {
  const { token } = useParams();
  const [shareData, setShareData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadShare();
  }, [token]);

  const loadShare = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await shareApi.getShareByToken(token);
      setShareData(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Share link is invalid, expired, or revoked.');
    } finally {
      setLoading(false);
    }
  };

  const handleDownload = async () => {
    try {
      toast.loading('Starting public file download...', { id: 'pubDl' });
      await shareApi.downloadSharedFile(token, shareData.originalFilename);
      toast.success('Download started!', { id: 'pubDl' });
    } catch (err) {
      toast.error('Download failed or permission denied', { id: 'pubDl' });
    }
  };

  return (
    <div className="min-h-screen flex flex-col justify-center items-center p-6 bg-panel/30">
      <div className="w-full max-w-lg bg-base p-8 rounded-3xl shadow-card border border-surface space-y-6">
        {/* Logo Header */}
        <div className="text-center space-y-2">
          <div className="w-12 h-12 rounded-2xl bg-panel flex items-center justify-center mx-auto shadow-soft border border-surface">
            <Sparkles className="w-6 h-6 text-accent-blue" />
          </div>
          <h1 className="font-bold text-2xl tracking-tight">
            Dis<span className="gemini-gradient-text">FileSys</span> Shared Content
          </h1>
          <p className="text-xs text-muted">Public Access Gateway</p>
        </div>

        {loading ? (
          <div className="py-12 flex flex-col items-center justify-center space-y-3">
            <div className="w-8 h-8 border-3 border-accent-blue border-t-transparent rounded-full animate-spin" />
            <p className="text-xs text-muted">Loading shared item...</p>
          </div>
        ) : error ? (
          <div className="p-6 bg-accent-red/5 border border-accent-red/20 rounded-2xl text-center space-y-3">
            <div className="w-10 h-10 rounded-xl bg-accent-red/10 text-accent-red flex items-center justify-center mx-auto">
              <ShieldAlert className="w-5 h-5" />
            </div>
            <h3 className="font-bold text-base text-charcoal">Access Denied</h3>
            <p className="text-xs text-muted">{error}</p>
          </div>
        ) : (
          <div className="space-y-6">
            <div className="p-5 bg-surface/30 rounded-2xl border border-surface flex items-center space-x-4">
              <div className="w-12 h-12 rounded-2xl bg-base flex items-center justify-center shadow-soft">
                {getFileIcon(shareData.contentType, shareData.originalFilename)}
              </div>
              <div className="min-w-0 flex-1">
                <h3 className="font-bold text-sm text-charcoal truncate" title={shareData.originalFilename}>
                  {shareData.originalFilename}
                </h3>
                <p className="text-xs text-muted mt-0.5">
                  Size: {formatBytes(shareData.sizeBytes)} • Permission: {shareData.permission}
                </p>
              </div>
            </div>

            {shareData.permission === 'DOWNLOAD' ? (
              <button
                onClick={handleDownload}
                className="w-full py-3 bg-accent-blue hover:bg-accent-blue/90 text-white font-semibold text-sm rounded-xl shadow-card transition-all flex items-center justify-center space-x-2"
              >
                <Download className="w-4 h-4" />
                <span>Download Shared File</span>
              </button>
            ) : (
              <div className="p-4 bg-accent-yellow/10 border border-accent-yellow/30 rounded-xl text-center text-xs text-charcoal">
                This share link has <strong>VIEW ONLY</strong> permission. Downloading is disabled by the owner.
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
