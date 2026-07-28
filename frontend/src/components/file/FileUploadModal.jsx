import React, { useState, useRef } from 'react';
import { UploadCloud, File, X, CheckCircle, AlertCircle, CopyCheck } from 'lucide-react';
import { fileApi } from '../../api/fileApi';
import toast from 'react-hot-toast';

export default function FileUploadModal({ isOpen, onClose, folderId, onUploadSuccess }) {
  const [dragActive, setDragActive] = useState(false);
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);
  const fileInputRef = useRef(null);

  if (!isOpen) return null;

  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      setSelectedFile(e.dataTransfer.files[0]);
    }
  };

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0]);
    }
  };

  const handleUpload = async () => {
    if (!selectedFile) return;

    setUploading(true);
    setProgress(0);

    try {
      const res = await fileApi.uploadFile(selectedFile, folderId, (pct) => setProgress(pct));
      const fileData = res.data;

      if (fileData?.deduplicated) {
        toast.success(
          <div className="flex items-center space-x-2">
            <CopyCheck className="w-4 h-4 text-accent-yellow" />
            <span>SHA-256 Deduplicated! Upload completed instantly.</span>
          </div>,
          { duration: 4000 }
        );
      } else {
        toast.success('File uploaded successfully!');
      }

      onUploadSuccess();
      onClose();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to upload file');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-charcoal/40 backdrop-blur-xs animate-fadeIn">
      <div className="bg-base rounded-2xl max-w-lg w-full p-6 shadow-card border border-surface space-y-6">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-accent-blue/10 text-accent-blue flex items-center justify-center">
              <UploadCloud className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-bold text-lg text-charcoal">Upload File</h3>
              <p className="text-xs text-muted">Stream file to S3 with SHA-256 deduplication</p>
            </div>
          </div>
          <button onClick={onClose} disabled={uploading} className="p-1 text-muted hover:text-charcoal rounded-lg">
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Drag & Drop Zone */}
        <div
          onDragEnter={handleDrag}
          onDragOver={handleDrag}
          onDragLeave={handleDrag}
          onDrop={handleDrop}
          onClick={() => fileInputRef.current?.click()}
          className={`border-2 border-dashed rounded-2xl p-8 text-center cursor-pointer transition-all duration-200 ${
            dragActive
              ? 'border-accent-blue bg-accent-blue/5 scale-[0.99]'
              : 'border-surface/80 hover:border-accent-blue/50 hover:bg-surface/30'
          }`}
        >
          <input
            ref={fileInputRef}
            type="file"
            onChange={handleFileChange}
            className="hidden"
          />

          <div className="w-12 h-12 rounded-2xl bg-panel flex items-center justify-center mx-auto mb-3 text-accent-blue shadow-soft">
            <UploadCloud className="w-6 h-6" />
          </div>

          <p className="font-semibold text-sm text-charcoal">
            Click to browse <span className="font-normal text-muted">or drag & drop</span>
          </p>
          <p className="text-[11px] text-muted mt-1">Any file up to 500MB supported</p>
        </div>

        {/* Selected File Info & Progress */}
        {selectedFile && (
          <div className="bg-surface/40 p-4 rounded-xl space-y-3 border border-surface/60">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-3 truncate">
                <File className="w-5 h-5 text-accent-blue shrink-0" />
                <div className="truncate">
                  <p className="text-xs font-semibold text-charcoal truncate">{selectedFile.name}</p>
                  <p className="text-[10px] text-muted">{(selectedFile.size / (1024 * 1024)).toFixed(2)} MB</p>
                </div>
              </div>
              {!uploading && (
                <button onClick={() => setSelectedFile(null)} className="text-muted hover:text-accent-red p-1">
                  <X className="w-4 h-4" />
                </button>
              )}
            </div>

            {/* Progress Bar */}
            {uploading && (
              <div className="space-y-1">
                <div className="w-full bg-surface rounded-full h-2 overflow-hidden">
                  <div
                    className="bg-accent-blue h-full transition-all duration-300 rounded-full"
                    style={{ width: `${progress}%` }}
                  />
                </div>
                <div className="flex justify-between text-[10px] font-semibold text-muted">
                  <span>Uploading...</span>
                  <span>{progress}%</span>
                </div>
              </div>
            )}
          </div>
        )}

        {/* Buttons */}
        <div className="flex items-center justify-end space-x-3">
          <button
            type="button"
            onClick={onClose}
            disabled={uploading}
            className="px-4 py-2 text-sm font-medium text-charcoal hover:bg-surface rounded-xl transition-colors disabled:opacity-50"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={handleUpload}
            disabled={!selectedFile || uploading}
            className="px-5 py-2 text-sm font-semibold text-white bg-accent-blue hover:bg-accent-blue/90 rounded-xl shadow-card transition-colors disabled:opacity-50 flex items-center space-x-2"
          >
            {uploading ? (
              <>
                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                <span>Uploading...</span>
              </>
            ) : (
              <span>Start Upload</span>
            )}
          </button>
        </div>
      </div>
    </div>
  );
}
