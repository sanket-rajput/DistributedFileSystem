import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Sparkles, Mail, Lock, UserCheck, ArrowRight } from 'lucide-react';
import { useAuthStore } from '../store/useAuthStore';
import toast from 'react-hot-toast';

export default function RegisterPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [role, setRole] = useState('USER');
  const { register, loading } = useAuthStore();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!email || !password) {
      toast.error('Please fill in all required fields');
      return;
    }

    if (password !== confirmPassword) {
      toast.error('Passwords do not match');
      return;
    }

    try {
      await register(email, password, role);
      toast.success('Account created successfully!');
      navigate('/dashboard');
    } catch (err) {
      toast.error(err.message || 'Registration failed');
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
          <p className="text-xs text-muted">Create a new account</p>
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

          <div>
            <label className="block text-xs font-semibold text-charcoal mb-1">Confirm Password</label>
            <div className="relative">
              <Lock className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-muted" />
              <input
                type="password"
                required
                placeholder="••••••••"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                className="w-full bg-surface/40 focus:bg-base text-sm pl-10 pr-4 py-2.5 rounded-xl border border-surface focus:border-accent-blue outline-none transition-all text-charcoal"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-charcoal mb-1">Role</label>
            <div className="grid grid-cols-2 gap-3">
              <button
                type="button"
                onClick={() => setRole('USER')}
                className={`py-2 rounded-xl text-xs font-semibold border transition-all ${
                  role === 'USER'
                    ? 'bg-accent-blue/10 border-accent-blue text-accent-blue'
                    : 'border-surface/80 text-muted hover:border-surface'
                }`}
              >
                USER
              </button>
              <button
                type="button"
                onClick={() => setRole('ADMIN')}
                className={`py-2 rounded-xl text-xs font-semibold border transition-all ${
                  role === 'ADMIN'
                    ? 'bg-accent-green/10 border-accent-green text-accent-green'
                    : 'border-surface/80 text-muted hover:border-surface'
                }`}
              >
                ADMIN
              </button>
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
                <span>Create Account</span>
                <UserCheck className="w-4 h-4" />
              </>
            )}
          </button>
        </form>

        <div className="text-center text-xs text-muted pt-2 border-t border-surface/60">
          Already have an account?{' '}
          <Link to="/login" className="font-semibold text-accent-blue hover:underline">
            Sign in
          </Link>
        </div>
      </div>
    </div>
  );
}
