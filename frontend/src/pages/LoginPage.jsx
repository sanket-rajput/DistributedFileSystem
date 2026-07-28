import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Sparkles, Mail, Lock, ArrowRight } from 'lucide-react';
import { useAuthStore } from '../store/useAuthStore';
import toast from 'react-hot-toast';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const { login, loading } = useAuthStore();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!email || !password) {
      toast.error('Please enter email and password');
      return;
    }

    try {
      await login(email, password);
      toast.success('Logged in successfully!');
      navigate('/dashboard');
    } catch (err) {
      toast.error(err.message || 'Invalid credentials');
    }
  };

  return (
    <div className="min-h-screen flex flex-col justify-center items-center p-6 bg-panel/30">
      <div className="w-full max-w-md bg-base p-8 rounded-3xl shadow-card border border-surface space-y-6">
        {/* Brand Logo */}
        <div className="text-center space-y-2">
          <div className="w-12 h-12 rounded-2xl bg-panel flex items-center justify-center mx-auto shadow-soft border border-surface">
            <Sparkles className="w-6 h-6 text-accent-blue" />
          </div>
          <h1 className="font-bold text-2xl tracking-tight">
            Dis<span className="gemini-gradient-text">FileSys</span>
          </h1>
          <p className="text-xs text-muted">Distributed File Sharing Platform</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-charcoal mb-1">Email Address</label>
            <div className="relative">
              <Mail className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-muted" />
              <input
                type="email"
                required
                placeholder="user@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full bg-surface/40 focus:bg-base text-sm pl-10 pr-4 py-2.5 rounded-xl border border-surface focus:border-accent-blue outline-none transition-all text-charcoal"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-charcoal mb-1">Password</label>
            <div className="relative">
              <Lock className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-muted" />
              <input
                type="password"
                required
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full bg-surface/40 focus:bg-base text-sm pl-10 pr-4 py-2.5 rounded-xl border border-surface focus:border-accent-blue outline-none transition-all text-charcoal"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-3 bg-accent-blue hover:bg-accent-blue/90 text-white font-semibold rounded-xl shadow-card transition-all flex items-center justify-center space-x-2 text-sm disabled:opacity-50 mt-2"
          >
            {loading ? (
              <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : (
              <>
                <span>Sign In</span>
                <ArrowRight className="w-4 h-4" />
              </>
            )}
          </button>
        </form>

        <div className="text-center text-xs text-muted pt-2 border-t border-surface/60">
          Don't have an account?{' '}
          <Link to="/register" className="font-semibold text-accent-blue hover:underline">
            Register here
          </Link>
        </div>
      </div>
    </div>
  );
}
