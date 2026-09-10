import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getAttempts } from '../api';

function AttemptHistory() {
  const [attempts, setAttempts] = useState([]);

  useEffect(() => {
    getAttempts().then(data => {
      // Sort by newest first
      const sorted = [...data].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
      setAttempts(sorted);
    }).catch(console.error);
  }, []);

  return (
    <div className="flex-1 overflow-y-auto p-8" style={{ maxWidth: '1000px', margin: '0 auto', width: '100%' }}>
      <div className="mb-8">
        <h1 className="font-headline-xl text-headline-xl text-on-surface tracking-tight mb-2">Execution History</h1>
        <p className="text-on-surface-variant text-body-lg">Review your past architectural designs and AI evaluations.</p>
      </div>
      
      {attempts.length === 0 ? (
        <div className="p-12 text-center bg-surface-container-low border border-outline-variant rounded-lg">
          <span className="material-symbols-outlined text-[48px] text-outline mb-4">history</span>
          <div className="text-on-surface-variant font-headline-md">No architecture executions found in this workspace.</div>
        </div>
      ) : (
        <div className="space-y-3">
          {attempts.map(attempt => {
            const date = new Date(attempt.createdAt);
            const dateStr = date.toLocaleDateString();
            const timeStr = date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
            
            const isCompleted = attempt.status === 'COMPLETED';
            const isFailed = attempt.status === 'FAILED';
            
            return (
              <Link 
                to={`/attempts/${attempt.id}`} 
                key={attempt.id} 
                className="flex items-center justify-between p-4 bg-surface-container hover:bg-surface-bright border border-outline-variant hover:border-primary/30 transition-all rounded"
              >
                <div className="flex items-center gap-4">
                  <span className={`px-2 py-0.5 rounded text-label-md uppercase font-semibold ${
                    isCompleted ? 'bg-primary/20 text-primary' : 
                    isFailed ? 'bg-error/20 text-error' : 
                    'bg-warning/20 text-warning'
                  }`}>
                    {attempt.status}
                  </span>
                  <span className="text-on-surface font-headline-md">Problem #{attempt.problemId}</span>
                </div>
                
                <div className="flex items-center gap-4 text-on-surface-variant font-code-sm">
                  <span>{dateStr} <span className="opacity-50">{timeStr}</span></span>
                  <span className="material-symbols-outlined text-[16px]">chevron_right</span>
                </div>
              </Link>
            );
          })}
        </div>
      )}
    </div>
  );
}

export default AttemptHistory;
