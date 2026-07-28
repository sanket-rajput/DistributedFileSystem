import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { shareApi } from '../api/shareApi';
import FileViewer from '../components/file/FileViewer';
import { Sparkles, Download, ShieldAlert, CheckCircle, Lock } from 'lucide-react';
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
      // Backend returns ApiResponse<ShareResponseDto> where metadata is in res.data
      const payload = res.data || res;
      setShareData(payload);
    } catch (err) {
      setError(err.response?.data?.message || 'Share link is invalid, expired, or revoked.');
    } finally {
      setLoading(false);
    }
  };

  const handleDownload = async () => {
    const file = shareData?.file;
    try {
      toast.loading('Starting public file download...', { id: 'pubDl' });
      await shareApi.downloadSharedFile(token, file?.originalFilename);
      toast.success('Download started!', { id: 'pubDl' });
    } catch (err) {
      toast.error('Download failed or permission denied', { id: 'pubDl' });
    }
  };

  const file = shareData?.file;
  const permission = shareData?.permission || 'DOWNLOAD';
  const streamUrl = shareApi.getPublicStreamUrl(token, true);

  return (
    <div className="min-h-screen flex flex-col justify-center items-center p-4 sm:p-6 bg-panel/30">
      <div className="w-full max-w-4xl bg-base p-6 sm:p-8 rounded-3xl shadow-card border border-surface space-y-6">
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

        {/* State 1: Loading */}
        {loading ? (
          <div className="py-16 flex flex-col items-center justify-center space-y-3">
            <div className="w-8 h-8 border-3 border-accent-blue border-t-transparent rounded-full animate-spin" />
            <p className="text-xs text-muted">Loading shared content...</p>
          </div>
        ) : error ? (
          /* State 2: Error (Expired / Revoked / Invalid Token) */
          <div className="p-8 bg-accent-red/5 border border-accent-red/20 rounded-2xl text-center space-y-3 max-w-md mx-auto">
            <div className="w-12 h-12 rounded-2xl bg-accent-red/10 text-accent-red flex items-center justify-center mx-auto shadow-soft">
              <ShieldAlert className="w-6 h-6" />
            </div>
            <h3 className="font-bold text-base text-charcoal">Public Access Restricted</h3>
            <p className="text-xs text-muted leading-relaxed">{error}</p>
          </div>
        ) : (
          /* State 3: Valid Share Content */
          <div className="space-y-6">
            {/* Permission Banner */}
            <div
              className={`p-3.5 rounded-xl border flex items-center justify-between text-xs font-semibold ${
                permission === 'VIEW'
                  ? 'bg-accent-yellow/10 border-accent-yellow/30 text-charcoal'
                  : 'bg-accent-green/10 border-accent-green/30 text-charcoal'
              }`}
            >
              <div className="flex items-center space-x-2">
                {permission === 'VIEW' ? (
                  <Lock className="w-4 h-4 text-accent-yellow" />
                ) : (
                  <CheckCircle className="w-4 h-4 text-accent-green" />
                )}
                <span>
                  {permission === 'VIEW'
                    ? 'VIEW ONLY ACCESS: Downloading is disabled by the file owner.'
                    : 'DOWNLOAD PERMISSION GRANTED: File preview and download are enabled.'}
                </span>
              </div>
            </div>

            {/* Inline File Viewer */}
            <FileViewer
              file={file}
              streamUrl={streamUrl}
              permission={permission}
              onDownload={permission === 'DOWNLOAD' ? handleDownload : null}
            />
          </div>
        )}
      </div>
    </div>
  );
}
