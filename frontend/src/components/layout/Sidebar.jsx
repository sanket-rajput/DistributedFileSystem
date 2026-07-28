import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { Folder, Search, ShieldCheck, LogOut, HardDrive, Sparkles, User } from 'lucide-react';
import { useAuthStore } from '../../store/useAuthStore';

export default function Sidebar() {
  const { user, logout, isAdmin } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <aside className="w-64 bg-panel border-r border-surface/50 flex flex-col justify-between h-screen sticky top-0 p-5 shadow-soft z-20">
      <div className="space-y-8">
        {/* Brand Logo & Wordmark */}
        <div className="flex items-center space-x-3 px-2">
          <div className="w-10 h-10 rounded-xl bg-base flex items-center justify-center shadow-card border border-surface">
            <Sparkles className="w-5 h-5 text-accent-blue" />
          </div>
          <div>
            <h1 className="font-bold text-lg leading-tight tracking-tight">
              Dis<span className="gemini-gradient-text">FileSys</span>
            </h1>
            <p className="text-[11px] font-medium text-muted">Distributed Storage</p>
          </div>
        </div>

        {/* Navigation Section */}
        <nav className="space-y-1">
          <NavLink
            to="/dashboard"
            className={({ isActive }) =>
              `flex items-center space-x-3 px-3.5 py-2.5 rounded-xl font-medium text-sm transition-all duration-200 ${
                isActive
                  ? 'bg-base text-accent-blue shadow-card font-semibold'
                  : 'text-charcoal/80 hover:bg-surface/60 hover:text-charcoal'
              }`
            }
          >
            <Folder className="w-4 h-4" />
            <span>My Files</span>
          </NavLink>

          <NavLink
            to="/search"
            className={({ isActive }) =>
              `flex items-center space-x-3 px-3.5 py-2.5 rounded-xl font-medium text-sm transition-all duration-200 ${
                isActive
                  ? 'bg-base text-accent-blue shadow-card font-semibold'
                  : 'text-charcoal/80 hover:bg-surface/60 hover:text-charcoal'
              }`
            }
          >
            <Search className="w-4 h-4" />
            <span>Search & Filter</span>
          </NavLink>

          {isAdmin() && (
            <NavLink
              to="/audit-logs"
              className={({ isActive }) =>
                `flex items-center space-x-3 px-3.5 py-2.5 rounded-xl font-medium text-sm transition-all duration-200 ${
                  isActive
                    ? 'bg-base text-accent-blue shadow-card font-semibold'
                    : 'text-charcoal/80 hover:bg-surface/60 hover:text-charcoal'
                }`
              }
            >
              <ShieldCheck className="w-4 h-4 text-accent-green" />
              <span>Audit Logs</span>
              <span className="ml-auto text-[10px] bg-accent-green/10 text-accent-green px-2 py-0.5 rounded-full font-bold uppercase">
                Admin
              </span>
            </NavLink>
          )}
        </nav>
      </div>

      {/* Footer User Profile & Logout */}
      <div className="pt-4 border-t border-surface/70">
        <div className="flex items-center justify-between p-2 rounded-xl bg-base/60 backdrop-blur-sm border border-surface/50">
          <div className="flex items-center space-x-3 min-w-0">
            <div className="w-8 h-8 rounded-full bg-accent-blue/10 text-accent-blue flex items-center justify-center font-bold text-xs shrink-0">
              {user?.email?.charAt(0).toUpperCase() || 'U'}
            </div>
            <div className="min-w-0">
              <p className="text-xs font-semibold text-charcoal truncate">{user?.email || 'User'}</p>
              <span className="text-[10px] text-muted capitalize">{user?.role || 'User'}</span>
            </div>
          </div>
          <button
            onClick={handleLogout}
            title="Logout"
            className="p-1.5 text-muted hover:text-accent-red hover:bg-accent-red/10 rounded-lg transition-colors"
          >
            <LogOut className="w-4 h-4" />
          </button>
        </div>
      </div>
    </aside>
  );
}
