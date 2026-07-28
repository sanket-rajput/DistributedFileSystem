import React, { useState, useEffect } from 'react';
import Header from '../components/layout/Header';
import FileGrid from '../components/file/FileGrid';
import { fileApi } from '../api/fileApi';
import { Search, Filter, RefreshCw, X } from 'lucide-react';
import toast from 'react-hot-toast';

export default function SearchPage() {
  const [name, setName] = useState('');
  const [contentType, setContentType] = useState('');
  const [minSize, setMinSize] = useState('');
  const [maxSize, setMaxSize] = useState('');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');

  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [totalElements, setTotalElements] = useState(0);

  const handleSearch = async (e) => {
    if (e) e.preventDefault();

    setLoading(true);
    try {
      const params = {};
      if (name.trim()) params.name = name.trim();
      if (contentType) params.contentType = contentType;
      if (minSize) params.minSize = parseInt(minSize) * 1024; // in KB
      if (maxSize) params.maxSize = parseInt(maxSize) * 1024;
      if (fromDate) params.fromDate = new Date(fromDate).toISOString();
      if (toDate) params.toDate = new Date(toDate).toISOString();

      const res = await fileApi.searchFiles(params);
      const pageData = res.data;
      setResults(pageData?.content || []);
      setTotalElements(pageData?.totalElements || 0);
    } catch (err) {
      toast.error('Search query failed');
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setName('');
    setContentType('');
    setMinSize('');
    setMaxSize('');
    setFromDate('');
    setToDate('');
    setResults([]);
    setTotalElements(0);
  };

  return (
    <div className="space-y-6">
      <Header searchQuery={name} setSearchQuery={setName} />

      <div className="bg-panel/40 p-6 rounded-3xl border border-surface shadow-soft space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <Filter className="w-4 h-4 text-accent-blue" />
            <h3 className="font-bold text-sm text-charcoal">Advanced Specification Filters</h3>
          </div>
          <button
            onClick={handleReset}
            className="text-xs font-semibold text-muted hover:text-charcoal flex items-center space-x-1"
          >
            <RefreshCw className="w-3.5 h-3.5" />
            <span>Reset Filters</span>
          </button>
        </div>

        <form onSubmit={handleSearch} className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
          <div>
            <label className="block text-[11px] font-semibold text-charcoal mb-1">File Name</label>
            <input
              type="text"
              placeholder="e.g. document.pdf"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full bg-base text-xs px-3.5 py-2.5 rounded-xl border border-surface focus:border-accent-blue outline-none text-charcoal"
            />
          </div>

          <div>
            <label className="block text-[11px] font-semibold text-charcoal mb-1">Content Type</label>
            <select
              value={contentType}
              onChange={(e) => setContentType(e.target.value)}
              className="w-full bg-base text-xs px-3.5 py-2.5 rounded-xl border border-surface focus:border-accent-blue outline-none text-charcoal"
            >
              <option value="">All Types</option>
              <option value="application/pdf">PDF Document</option>
              <option value="image/jpeg">JPEG Image</option>
              <option value="image/png">PNG Image</option>
              <option value="text/plain">Text File</option>
              <option value="application/zip">ZIP Archive</option>
            </select>
          </div>

          <div>
            <label className="block text-[11px] font-semibold text-charcoal mb-1">Min Size (KB)</label>
            <input
              type="number"
              placeholder="e.g. 100"
              value={minSize}
              onChange={(e) => setMinSize(e.target.value)}
              className="w-full bg-base text-xs px-3.5 py-2.5 rounded-xl border border-surface focus:border-accent-blue outline-none text-charcoal"
            />
          </div>

          <div>
            <label className="block text-[11px] font-semibold text-charcoal mb-1">Max Size (KB)</label>
            <input
              type="number"
              placeholder="e.g. 5000"
              value={maxSize}
              onChange={(e) => setMaxSize(e.target.value)}
              className="w-full bg-base text-xs px-3.5 py-2.5 rounded-xl border border-surface focus:border-accent-blue outline-none text-charcoal"
            />
          </div>

          <div>
            <label className="block text-[11px] font-semibold text-charcoal mb-1">From Date</label>
            <input
              type="date"
              value={fromDate}
              onChange={(e) => setFromDate(e.target.value)}
              className="w-full bg-base text-xs px-3.5 py-2.5 rounded-xl border border-surface focus:border-accent-blue outline-none text-charcoal"
            />
          </div>

          <div>
            <label className="block text-[11px] font-semibold text-charcoal mb-1">To Date</label>
            <input
              type="date"
              value={toDate}
              onChange={(e) => setToDate(e.target.value)}
              className="w-full bg-base text-xs px-3.5 py-2.5 rounded-xl border border-surface focus:border-accent-blue outline-none text-charcoal"
            />
          </div>

          <div className="sm:col-span-2 md:col-span-2 flex items-end">
            <button
              type="submit"
              disabled={loading}
              className="w-full py-2.5 bg-accent-blue hover:bg-accent-blue/90 text-white font-semibold text-xs rounded-xl shadow-card transition-all flex items-center justify-center space-x-2"
            >
              {loading ? (
                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
              ) : (
                <>
                  <Search className="w-4 h-4" />
                  <span>Execute Specification Search</span>
                </>
              )}
            </button>
          </div>
        </form>
      </div>

      {/* Results */}
      <div>
        <div className="flex items-center justify-between mb-4">
          <h3 className="font-bold text-sm text-charcoal">Search Results ({totalElements})</h3>
        </div>

        {loading ? (
          <div className="py-16 text-center text-xs text-muted">Searching repository...</div>
        ) : (
          <FileGrid
            files={results}
            onFileDownload={(file) => fileApi.downloadFile(file.id, file.originalFilename)}
          />
        )}
      </div>
    </div>
  );
}
