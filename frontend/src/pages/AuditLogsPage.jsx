import React, { useState, useEffect } from 'react';
import Header from '../components/layout/Header';
import { auditApi } from '../api/auditApi';
import { ShieldCheck, Filter, ChevronLeft, ChevronRight } from 'lucide-react';
import toast from 'react-hot-toast';

export default function AuditLogsPage() {
  const [logs, setLogs] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [actionFilter, setActionFilter] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadAuditLogs();
  }, [page, actionFilter]);

  const loadAuditLogs = async () => {
    setLoading(true);
    try {
      const params = { page, size: 15 };
      if (actionFilter) params.action = actionFilter;

      const res = await auditApi.getAuditLogs(params);
      const pageData = res.data;
      setLogs(pageData?.content || []);
      setTotalPages(pageData?.totalPages || 0);
    } catch (err) {
      toast.error('Failed to load audit logs (Admin access required)');
    } finally {
      setLoading(false);
    }
  };

  const getActionBadgeClass = (action = '') => {
    if (action.includes('UPLOAD')) return 'bg-accent-blue/10 text-accent-blue';
    if (action.includes('DELETE')) return 'bg-accent-red/10 text-accent-red';
    if (action.includes('SHARE')) return 'bg-accent-green/10 text-accent-green';
    return 'bg-accent-yellow/10 text-accent-yellow';
  };

  return (
    <div className="space-y-6">
      <Header />

      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-3">
          <div className="w-10 h-10 rounded-xl bg-accent-green/10 text-accent-green flex items-center justify-center">
            <ShieldCheck className="w-5 h-5" />
          </div>
          <div>
            <h2 className="font-bold text-xl text-charcoal">System Audit Trail</h2>
            <p className="text-xs text-muted">Admin-only cross-cutting operation audit logs</p>
          </div>
        </div>

        {/* Action Filter */}
        <div className="flex items-center space-x-2">
          <Filter className="w-4 h-4 text-muted" />
          <select
            value={actionFilter}
            onChange={(e) => { setActionFilter(e.target.value); setPage(0); }}
            className="bg-base text-xs font-semibold px-3 py-2 rounded-xl border border-surface text-charcoal outline-none shadow-soft"
          >
            <option value="">All Actions</option>
            <option value="UPLOAD">UPLOAD</option>
            <option value="DOWNLOAD">DOWNLOAD</option>
            <option value="DELETE">DELETE</option>
            <option value="SHARE">SHARE</option>
            <option value="VERSION_RESTORE">VERSION_RESTORE</option>
            <option value="REVOKE_SHARE">REVOKE_SHARE</option>
          </select>
        </div>
      </div>

      {/* Audit Log Table */}
      <div className="bg-base rounded-2xl border border-surface/70 shadow-soft overflow-hidden">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-surface/30 text-muted text-xs font-semibold uppercase tracking-wider border-b border-surface/60">
              <th className="py-3.5 px-4">Timestamp</th>
              <th className="py-3.5 px-4">User Email</th>
              <th className="py-3.5 px-4">Action</th>
              <th className="py-3.5 px-4">Resource Type</th>
              <th className="py-3.5 px-4">Details</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-surface/40 text-xs">
            {loading ? (
              <tr>
                <td colSpan="5" className="py-12 text-center text-muted">Loading audit trail...</td>
              </tr>
            ) : logs.length === 0 ? (
              <tr>
                <td colSpan="5" className="py-12 text-center text-muted">No audit logs found.</td>
              </tr>
            ) : (
              logs.map((log) => (
                <tr key={log.id} className="hover:bg-surface/20 transition-colors">
                  <td className="py-3 px-4 font-mono text-muted">{new Date(log.timestamp).toLocaleString()}</td>
                  <td className="py-3 px-4 font-medium text-charcoal">{log.userEmail || 'System'}</td>
                  <td className="py-3 px-4">
                    <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${getActionBadgeClass(log.action)}`}>
                      {log.action}
                    </span>
                  </td>
                  <td className="py-3 px-4 text-muted">{log.resourceType}</td>
                  <td className="py-3 px-4 text-charcoal truncate max-w-xs" title={log.details}>
                    {log.details}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>

        {/* Pagination Footer */}
        <div className="px-4 py-3 bg-panel/30 border-t border-surface/60 flex items-center justify-between">
          <span className="text-xs text-muted">
            Page <span className="font-bold text-charcoal">{page + 1}</span> of <span className="font-bold text-charcoal">{totalPages || 1}</span>
          </span>
          <div className="flex items-center space-x-2">
            <button
              onClick={() => setPage(Math.max(0, page - 1))}
              disabled={page === 0}
              className="p-1.5 rounded-lg border border-surface bg-base text-charcoal hover:bg-surface disabled:opacity-40"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <button
              onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
              disabled={page >= totalPages - 1}
              className="p-1.5 rounded-lg border border-surface bg-base text-charcoal hover:bg-surface disabled:opacity-40"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
