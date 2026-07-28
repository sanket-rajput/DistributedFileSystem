import React, { useState } from 'react';
import { Share2, X, Copy, Check, Eye, Download, ShieldAlert, Calendar } from 'lucide-react';
import { shareApi } from '../../api/shareApi';
import toast from 'react-hot-toast';

export default function ShareModal({ file, isOpen, onClose }) {
  const [permission, setPermission] = useState('DOWNLOAD');
  const [expiryDate, setExpiryDate] = useState('');
  const [loading, setLoading] = useState(false);
  const [activeShare, setActiveShare] = useState(null);
  const [copied, setCopied] = useState(false);

  if (!isOpen || !file) return null;

  const handleCreateShare = async (e) => {
    e.preventDefault();
    setLoading(true);

    let formattedExpiry = null;
    if (expiryDate) {
      formattedExpiry = new Date(expiryDate).toISOString();
    }

    try {
      const res = await shareApi.createShare(file.id, formattedExpiry, permission);
      const shareData = res.data;
      setActiveShare(shareData);
      toast.success('Public share link generated!');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to generate share link');
    } finally {
      setLoading(false);
    }
  };

  const handleCopy = (token) => {
    const publicUrl = `${window.location.origin}/share/${token}`;
    navigator.clipboard.writeText(publicUrl);
    setCopied(true);
    toast.success('Link copied to clipboard!');
    setTimeout(() => setCopied(false), 2000);
  };

  const handleRevoke = async () => {
    if (!activeShare) return;
    try {
      await shareApi.revokeShare(file.id, activeShare.id);
      setActiveShare(null);
      toast.success('Share link revoked');
    } catch (err) {
      toast.error('Failed to revoke share link');
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-charcoal/40 backdrop-blur-xs animate-fadeIn">
      <div className="bg-base rounded-2xl max-w-lg w-full p-6 shadow-card border border-surface space-y-6">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-accent-green/10 text-accent-green flex items-center justify-center">
              <Share2 className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-bold text-lg text-charcoal">Share File</h3>
              <p className="text-xs text-muted">Generate secure public link for "{file.originalFilename}"</p>
            </div>
          </div>
          <button onClick={onClose} className="p-1 text-muted hover:text-charcoal rounded-lg">
            <X className="w-4 h-4" />
          </button>
        </div>

        {!activeShare ? (
          <form onSubmit={handleCreateShare} className="space-y-4">
            {/* Permission Toggle */}
            <div>
              <label className="block text-xs font-semibold text-charcoal mb-2">Access Permission</label>
              <div className="grid grid-cols-2 gap-3">
                <button
                  type="button"
                  onClick={() => setPermission('VIEW')}
                  className={`p-3 rounded-xl border flex items-center space-x-2.5 transition-all ${
                    permission === 'VIEW'
                      ? 'bg-accent-blue/5 border-accent-blue text-accent-blue font-semibold shadow-soft'
                      : 'border-surface/80 hover:border-surface text-charcoal/80'
                  }`}
                >
                  <Eye className="w-4 h-4" />
                  <span className="text-xs">View Metadata Only</span>
                </button>
                <button
                  type="button"
                  onClick={() => setPermission('DOWNLOAD')}
                  className={`p-3 rounded-xl border flex items-center space-x-2.5 transition-all ${
                    permission === 'DOWNLOAD'
                      ? 'bg-accent-blue/5 border-accent-blue text-accent-blue font-semibold shadow-soft'
                      : 'border-surface/80 hover:border-surface text-charcoal/80'
                  }`}
                >
                  <Download className="w-4 h-4" />
                  <span className="text-xs">Allow Download</span>
                </button>
              </div>
            </div>

            {/* Optional Expiry Date Picker */}
            <div>
              <label className="block text-xs font-semibold text-charcoal mb-1">Optional Expiration Date</label>
              <div className="relative">
                <input
                  type="datetime-local"
                  value={expiryDate}
                  onChange={(e) => setExpiryDate(e.target.value)}
                  className="w-full bg-surface/40 focus:bg-base text-xs px-3.5 py-2.5 rounded-xl border border-surface focus:border-accent-blue outline-none text-charcoal"
                />
              </div>
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
                disabled={loading}
                className="px-5 py-2 text-sm font-semibold text-white bg-accent-green hover:bg-accent-green/90 rounded-xl shadow-card transition-colors flex items-center space-x-2"
              >
                {loading ? 'Generating...' : 'Create Public Link'}
              </button>
            </div>
          </form>
        ) : (
          <div className="space-y-4">
            <div className="bg-accent-green/5 border border-accent-green/20 p-4 rounded-2xl space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-accent-green uppercase">Active Share Link</span>
                <span className="text-[11px] font-semibold text-muted">Permission: {activeShare.permission}</span>
              </div>

              <div className="flex items-center space-x-2">
                <input
                  type="text"
                  readOnly
                  value={`${window.location.origin}/share/${activeShare.token}`}
                  className="w-full bg-base text-xs px-3 py-2 rounded-xl border border-surface text-charcoal font-mono truncate"
                />
                <button
                  onClick={() => handleCopy(activeShare.token)}
                  className="px-3 py-2 bg-accent-blue text-white rounded-xl text-xs font-semibold hover:bg-accent-blue/90 shrink-0 flex items-center space-x-1"
                >
                  {copied ? <Check className="w-3.5 h-3.5" /> : <Copy className="w-3.5 h-3.5" />}
                  <span>{copied ? 'Copied' : 'Copy'}</span>
                </button>
              </div>

              {activeShare.expiresAt && (
                <p className="text-[11px] text-muted">
                  Expires on {new Date(activeShare.expiresAt).toLocaleString()}
                </p>
              )}
            </div>

            <div className="flex items-center justify-between pt-2">
              <button
                type="button"
                onClick={handleRevoke}
                className="px-4 py-2 text-xs font-semibold text-accent-red bg-accent-red/10 hover:bg-accent-red/20 rounded-xl transition-colors"
              >
                Revoke Share Link
              </button>
              <button
                type="button"
                onClick={onClose}
                className="px-4 py-2 text-xs font-semibold text-charcoal hover:bg-surface rounded-xl transition-colors"
              >
                Done
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
