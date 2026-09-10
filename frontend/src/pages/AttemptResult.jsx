import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getAttempt } from '../api';

function AttemptResult() {
  const { id } = useParams();
  const [attempt, setAttempt] = useState(null);

  useEffect(() => {
    const fetchAttempt = () => {
      getAttempt(id).then(res => {
        setAttempt(res);
      }).catch(console.error);
    };

    fetchAttempt();

    const intervalId = setInterval(() => {
      setAttempt(prev => {
        if (!prev || prev.status === 'EVALUATING' || prev.status === 'SUBMITTED') {
          fetchAttempt();
        }
        return prev;
      });
    }, 3000);

    return () => clearInterval(intervalId);
  }, [id]);

  if (!attempt) return <div className="p-8 text-on-surface-variant font-code-md">Loading execution results...</div>;

  return (
    <div className="flex-1 overflow-y-auto p-8 font-code-sm text-code-sm bg-surface-container-lowest" style={{ maxWidth: '1000px', margin: '0 auto', width: '100%' }}>
      <div className="flex items-center justify-between mb-8 pb-4 border-b border-border-default">
        <h1 className="font-headline-xl text-headline-xl text-on-surface">Execution Result</h1>
        <span className={`px-2 py-1 rounded-sm text-label-md uppercase font-semibold ${
          attempt.status === 'COMPLETED' ? 'bg-primary/20 text-primary' : 
          attempt.status === 'FAILED' ? 'bg-error/20 text-error' : 
          'bg-warning/20 text-warning'
        }`}>{attempt.status}</span>
      </div>

      <div className="space-y-1 mb-8 bg-surface-container p-4 rounded border border-outline-variant">
        <div className="text-on-surface-variant font-label-md mb-2">// SUBMISSION LOG</div>
        <pre className="text-on-surface whitespace-pre-wrap">{attempt.content}</pre>
      </div>

      {(attempt.status === 'EVALUATING' || attempt.status === 'SUBMITTED') && (
        <div className="flex flex-col items-center justify-center p-12 bg-surface-container-low rounded border border-outline-variant mt-8">
           <span className="material-symbols-outlined text-primary text-[32px] mb-4 animate-spin">refresh</span>
           <div className="text-primary font-headline-md mb-2">Evaluating System Architecture...</div>
           <div className="text-on-surface-variant font-code-md">Compiling design patterns and running static analysis via LLM...</div>
        </div>
      )}

      {attempt.status === 'FAILED' && (
        <div className="flex items-start gap-4 p-4 bg-error/10 border border-error/30 rounded mt-8">
           <span className="material-symbols-outlined text-error text-[24px]">error</span>
           <div>
             <div className="text-error font-headline-md mb-1">Compilation Error</div>
             <div className="text-on-surface-variant mb-4">The evaluation pipeline encountered an error processing this request.</div>
             <Link to={`/problems/${attempt.problemId}`} className="px-4 py-2 bg-surface-container hover:bg-surface-bright text-on-surface rounded font-headline-sm transition-colors border border-outline-variant inline-block">Retry Implementation</Link>
           </div>
        </div>
      )}

      {attempt.status === 'COMPLETED' && attempt.evaluation && (
        <div className="mt-8 space-y-4">
          <div className="text-on-surface-variant font-label-md mb-4">// STATIC ANALYSIS REPORT</div>
          
          {attempt.evaluation.criteria.map((fb, i) => {
            const isHigh = fb.score >= 8;
            const isMedium = fb.score >= 5 && fb.score < 8;
            const isLow = fb.score < 5;
            
            const colorClass = isHigh ? 'text-primary' : (isMedium ? 'text-tertiary' : 'text-error');
            const bgClass = isHigh ? 'bg-primary/10' : (isMedium ? 'bg-tertiary/10' : 'bg-error/10');
            const borderClass = isHigh ? 'border-primary/30' : (isMedium ? 'border-tertiary/30' : 'border-error/30');

            return (
              <div key={i} className={`p-4 rounded border ${borderClass} bg-surface-container`}>
                <div className="flex items-center justify-between mb-2">
                  <span className={`font-headline-md ${colorClass}`}>{fb.criterion.replace(/_/g, ' ')}</span>
                  <span className={`px-2 py-0.5 rounded ${bgClass} ${colorClass} font-label-md font-bold`}>{fb.score} / 10</span>
                </div>
                
                <div className="space-y-2 mt-4">
                  {fb.concern && (
                    <div className="flex items-start gap-2">
                      <span className="text-warning font-label-sm mt-1 w-12 flex-shrink-0">WARN</span>
                      <span className="text-on-surface">{fb.concern}</span>
                    </div>
                  )}
                  {fb.suggestion && (
                    <div className="flex items-start gap-2">
                      <span className="text-primary font-label-sm mt-1 w-12 flex-shrink-0">FIX</span>
                      <span className="text-on-surface">{fb.suggestion}</span>
                    </div>
                  )}
                  {fb.evidence && (
                    <div className="mt-3 ml-14 p-2 bg-surface-container-lowest border border-outline-variant rounded text-on-surface-variant">
                      <span className="text-[#bc8cff] mr-2">&gt;</span>{fb.evidence}
                    </div>
                  )}
                </div>
              </div>
            );
          })}
          
          <div className="mt-8 pt-4 border-t border-border-default flex justify-end">
             <Link to={`/problems/${attempt.problemId}`} className="px-6 py-2 bg-surface-container hover:bg-surface-bright text-on-surface rounded font-headline-sm transition-colors border border-outline-variant">Improve Implementation</Link>
          </div>
        </div>
      )}
    </div>
  );
}

export default AttemptResult;
