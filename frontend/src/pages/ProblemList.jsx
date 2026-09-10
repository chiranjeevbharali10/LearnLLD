import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getProblems } from '../api';

function ProblemList() {
  const [problems, setProblems] = useState([]);

  useEffect(() => {
    getProblems().then(setProblems).catch(console.error);
  }, []);

  return (
    <div className="flex-1 overflow-y-auto p-8" style={{ maxWidth: '1200px', margin: '0 auto', width: '100%' }}>
      <div className="mb-8">
        <h1 className="font-headline-xl text-headline-xl text-on-surface tracking-tight mb-2">System Design Problems</h1>
        <p className="text-on-surface-variant text-body-lg">Select a component architecture challenge to begin your session.</p>
      </div>
      
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {problems.map(problem => (
          <Link to={`/problems/${problem.id}`} key={problem.id} className="group flex flex-col bg-surface-container-low border border-outline-variant hover:border-primary/50 hover:bg-surface-container transition-all rounded-lg p-6 h-full cursor-pointer">
            <h2 className="font-headline-lg text-headline-lg text-primary group-hover:text-primary-fixed-dim transition-colors mb-3">{problem.title}</h2>
            <p className="text-on-surface-variant font-body-md line-clamp-3 mb-6 flex-1">
              {problem.statement}
            </p>
            <div className="flex justify-end mt-auto">
              <span className="flex items-center gap-1 px-4 py-2 rounded bg-surface-container-high text-on-surface font-headline-sm transition-colors border border-outline-variant group-hover:bg-primary group-hover:text-surface-container-lowest group-hover:border-primary">
                Solve Challenge <span className="material-symbols-outlined text-[16px]">arrow_forward</span>
              </span>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}

export default ProblemList;
