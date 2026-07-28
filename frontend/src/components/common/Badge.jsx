import React from 'react';
import { CopyCheck, ShieldAlert, CheckCircle, Clock } from 'lucide-react';

export default function Badge({ type, text }) {
  if (type === 'duplicate') {
    return (
      <span className="inline-flex items-center space-x-1 px-2 py-0.5 rounded-full text-[11px] font-semibold bg-accent-yellow/15 text-charcoal border border-accent-yellow/30">
        <CopyCheck className="w-3 h-3 text-accent-yellow" />
        <span>Deduplicated</span>
      </span>
    );
  }

  if (type === 'version') {
    return (
      <span className="inline-flex items-center space-x-1 px-2 py-0.5 rounded-full text-[11px] font-semibold bg-accent-blue/10 text-accent-blue border border-accent-blue/20">
        <span>v{text}</span>
      </span>
    );
  }

  if (type === 'success') {
    return (
      <span className="inline-flex items-center space-x-1 px-2 py-0.5 rounded-full text-[11px] font-semibold bg-accent-green/10 text-accent-green border border-accent-green/20">
        <CheckCircle className="w-3 h-3" />
        <span>{text}</span>
      </span>
    );
  }

  if (type === 'danger') {
    return (
      <span className="inline-flex items-center space-x-1 px-2 py-0.5 rounded-full text-[11px] font-semibold bg-accent-red/10 text-accent-red border border-accent-red/20">
        <ShieldAlert className="w-3 h-3" />
        <span>{text}</span>
      </span>
    );
  }

  return (
    <span className="inline-flex items-center space-x-1 px-2 py-0.5 rounded-full text-[11px] font-semibold bg-surface text-muted border border-surface">
      <span>{text}</span>
    </span>
  );
}
