import React, { useState, useEffect } from 'react';
import {
  FileText,
  Image as ImageIcon,
  Video,
  Music,
  FileCode,
  Download,
  EyeOff,
  AlertTriangle,
  ZoomIn,
  ZoomOut,
  Copy,
  Check
} from 'lucide-react';
import { formatBytes } from './FileCard';
import toast from 'react-hot-toast';

export default function FileViewer({
  file,
  streamUrl,
  permission = 'DOWNLOAD',
  onDownload = null
}) {
  const [textContent, setTextContent] = useState('');
  const [loadingText, setLoadingText] = useState(false);
  const [textError, setTextError] = useState(false);
  const [imgZoomed, setImgZoomed] = useState(false);
  const [copiedCode, setCopiedCode] = useState(false);

  const contentType = (file?.contentType || '').toLowerCase();
  const filename = (file?.originalFilename || '').toLowerCase();

  // Determine media type
  const isImage = contentType.startsWith('image/') || /\.(jpg|jpeg|png|gif|webp|svg)$/i.test(filename);
  const isPdf = contentType === 'application/pdf' || filename.endsWith('.pdf');
  const isVideo = contentType.startsWith('video/') || /\.(mp4|webm|ogg|mov)$/i.test(filename);
  const isAudio = contentType.startsWith('audio/') || /\.(mp3|wav|ogg|m4a)$/i.test(filename);
  const isText =
    contentType.startsWith('text/') ||
    contentType.includes('json') ||
    contentType.includes('xml') ||
    contentType.includes('javascript') ||
    /\.(txt|md|json|csv|log|html|js|jsx|ts|tsx|py|java|c|cpp|css|yml|yaml)$/i.test(filename);

  useEffect(() => {
    if (isText && streamUrl) {
      fetchTextContent();
    }
  }, [isText, streamUrl]);

  const fetchTextContent = async () => {
    setLoadingText(true);
    setTextError(false);
    try {
      const res = await fetch(streamUrl);
      if (!res.ok) throw new Error('Failed to fetch text content');
      const text = await res.text();
      setTextContent(text);
    } catch (err) {
      setTextError(true);
    } finally {
      setLoadingText(false);
    }
  };

  const handleCopyCode = () => {
    navigator.clipboard.writeText(textContent);
    setCopiedCode(true);
    toast.success('Code copied to clipboard');
    setTimeout(() => setCopiedCode(false), 2000);
  };

  return (
    <div className="w-full bg-base rounded-2xl border border-surface shadow-card overflow-hidden flex flex-col">
      {/* File Header Bar */}
      <div className="p-4 bg-panel/50 border-b border-surface/70 flex items-center justify-between">
        <div className="flex items-center space-x-3 min-w-0">
          <div className="w-9 h-9 rounded-xl bg-accent-blue/10 text-accent-blue flex items-center justify-center shrink-0">
            {isImage && <ImageIcon className="w-4 h-4" />}
            {isPdf && <FileText className="w-4 h-4 text-accent-red" />}
            {isVideo && <Video className="w-4 h-4 text-accent-blue" />}
            {isAudio && <Music className="w-4 h-4 text-accent-yellow" />}
            {isText && <FileCode className="w-4 h-4 text-accent-green" />}
            {!isImage && !isPdf && !isVideo && !isAudio && !isText && <FileText className="w-4 h-4 text-muted" />}
          </div>
          <div className="min-w-0 truncate">
            <h4 className="font-bold text-xs text-charcoal truncate" title={file?.originalFilename}>
              {file?.originalFilename || 'Shared File'}
            </h4>
            <p className="text-[11px] text-muted truncate">
              {formatBytes(file?.sizeBytes)} • {file?.contentType || 'Unknown Type'}
            </p>
          </div>
        </div>

        {/* Header Action Controls */}
        <div className="flex items-center space-x-2 shrink-0">
          {isImage && (
            <button
              onClick={() => setImgZoomed(!imgZoomed)}
              className="p-2 text-muted hover:text-charcoal bg-surface/60 hover:bg-surface rounded-xl transition-colors text-xs font-semibold flex items-center space-x-1"
              title="Toggle Zoom"
            >
              {imgZoomed ? <ZoomOut className="w-3.5 h-3.5" /> : <ZoomIn className="w-3.5 h-3.5" />}
              <span className="hidden sm:inline">{imgZoomed ? 'Fit' : 'Zoom'}</span>
            </button>
          )}

          {isText && textContent && (
            <button
              onClick={handleCopyCode}
              className="p-2 text-muted hover:text-charcoal bg-surface/60 hover:bg-surface rounded-xl transition-colors text-xs font-semibold flex items-center space-x-1"
              title="Copy Text"
            >
              {copiedCode ? <Check className="w-3.5 h-3.5 text-accent-green" /> : <Copy className="w-3.5 h-3.5" />}
              <span className="hidden sm:inline">{copiedCode ? 'Copied' : 'Copy'}</span>
            </button>
          )}

          {permission === 'DOWNLOAD' && onDownload && (
            <button
              onClick={onDownload}
              className="px-3 py-1.5 bg-accent-blue hover:bg-accent-blue/90 text-white font-semibold rounded-xl text-xs shadow-card flex items-center space-x-1.5 transition-all"
            >
              <Download className="w-3.5 h-3.5" />
              <span>Download</span>
            </button>
          )}
        </div>
      </div>

      {/* Main Preview Container */}
      <div className="p-4 sm:p-6 flex items-center justify-center min-h-[320px] max-h-[650px] overflow-auto bg-surface/20">
        {/* 1. Image Viewer */}
        {isImage && (
          <div className="flex items-center justify-center w-full">
            <img
              src={streamUrl}
              alt={file?.originalFilename}
              className={`rounded-xl object-contain transition-all duration-300 shadow-soft ${
                imgZoomed ? 'max-w-none max-h-none cursor-zoom-out' : 'max-w-full max-h-[550px] cursor-zoom-in'
              }`}
              onClick={() => setImgZoomed(!imgZoomed)}
              onError={(e) => {
                e.target.style.display = 'none';
              }}
            />
          </div>
        )}

        {/* 2. PDF Viewer */}
        {isPdf && (
          <div className="w-full h-[550px] rounded-xl overflow-hidden shadow-soft border border-surface bg-base">
            <iframe
              src={streamUrl}
              title={file?.originalFilename}
              className="w-full h-full border-none"
            />
          </div>
        )}

        {/* 3. Video Viewer */}
        {isVideo && (
          <div className="w-full max-w-3xl flex items-center justify-center">
            <video
              controls
              controlsList="nodownload"
              src={streamUrl}
              className="w-full max-h-[500px] rounded-xl shadow-card bg-charcoal outline-none"
            >
              Your browser does not support HTML5 video playback.
            </video>
          </div>
        )}

        {/* 4. Audio Viewer */}
        {isAudio && (
          <div className="w-full max-w-md p-6 bg-base rounded-2xl border border-surface shadow-soft text-center space-y-4">
            <div className="w-16 h-16 rounded-2xl bg-accent-yellow/10 text-accent-yellow flex items-center justify-center mx-auto shadow-soft">
              <Music className="w-8 h-8" />
            </div>
            <div>
              <h4 className="font-bold text-sm text-charcoal">{file?.originalFilename}</h4>
              <p className="text-xs text-muted mt-1">{formatBytes(file?.sizeBytes)}</p>
            </div>
            <audio controls src={streamUrl} className="w-full outline-none">
              Your browser does not support audio playback.
            </audio>
          </div>
        )}

        {/* 5. Plain Text / Code Viewer */}
        {isText && (
          <div className="w-full h-full max-h-[550px] flex flex-col bg-charcoal text-panel p-4 rounded-xl font-mono text-xs overflow-auto shadow-card">
            {loadingText ? (
              <div className="py-12 flex flex-col items-center justify-center space-y-2 text-muted">
                <div className="w-5 h-5 border-2 border-accent-blue border-t-transparent rounded-full animate-spin" />
                <span>Loading text content...</span>
              </div>
            ) : textError ? (
              <div className="py-12 text-center text-accent-red space-y-1">
                <AlertTriangle className="w-6 h-6 mx-auto mb-1" />
                <p>Failed to load text preview</p>
              </div>
            ) : (
              <pre className="whitespace-pre-wrap break-words leading-relaxed">{textContent}</pre>
            )}
          </div>
        )}

        {/* 6. Unsupported File Type Fallback */}
        {!isImage && !isPdf && !isVideo && !isAudio && !isText && (
          <div className="text-center p-8 bg-base border border-surface/80 rounded-2xl max-w-sm space-y-4 shadow-soft">
            <div className="w-12 h-12 rounded-2xl bg-surface/60 text-muted flex items-center justify-center mx-auto">
              <EyeOff className="w-6 h-6" />
            </div>
            <div>
              <h4 className="font-bold text-sm text-charcoal">Preview Not Available</h4>
              <p className="text-xs text-muted mt-1">
                Inline preview is not supported for <span className="font-semibold text-charcoal">{file?.contentType || 'this file type'}</span>.
              </p>
            </div>

            {permission === 'DOWNLOAD' && onDownload ? (
              <button
                onClick={onDownload}
                className="w-full py-2.5 bg-accent-blue hover:bg-accent-blue/90 text-white font-semibold text-xs rounded-xl shadow-card transition-all flex items-center justify-center space-x-2"
              >
                <Download className="w-4 h-4" />
                <span>Download File</span>
              </button>
            ) : (
              <div className="p-3 bg-accent-yellow/10 border border-accent-yellow/30 rounded-xl text-[11px] text-charcoal font-medium">
                Downloading is disabled for this view-only share link.
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
