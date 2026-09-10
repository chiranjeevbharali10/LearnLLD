import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getProblem, createAttempt } from '../api';

function ProblemDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [problem, setProblem] = useState(null);
  const [content, setContent] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [activeLeftTab, setActiveLeftTab] = useState('desc');

  useEffect(() => {
    getProblem(id).then(setProblem).catch(console.error);
  }, [id]);

  const handleSubmit = async () => {
    if (content.length < 20) {
      setError('Submission is too short. Please provide a detailed design.');
      return;
    }
    
    setSubmitting(true);
    setError('');
    
    try {
      const attempt = await createAttempt(id, 'TEXT', content);
      navigate(`/attempts/${attempt.id}`);
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error || 'An error occurred during submission.';
      setError(msg);
      setSubmitting(false);
    }
  };

  if (!problem) return <div className="p-8 text-on-surface-variant">Loading workspace...</div>;

  return (
    <div className="flex flex-col w-full h-[calc(100vh-3.5rem)] bg-surface-container-lowest">
      {/* Interactive Split Workspace Container */}
      <div className="flex flex-col lg:flex-row w-full flex-1 overflow-hidden" id="workspace-container">
        
        {/* LEFT PANE: Problem Context */}
        <section className="w-full lg:w-[45%] flex flex-col h-full bg-surface-container-low overflow-hidden">
          
          {/* Tabs Bar */}
          <div className="flex items-center justify-between px-inset-md bg-surface-container-lowest shadow-sm flex-shrink-0 h-10">
            <div className="flex items-center gap-inset-xs overflow-x-auto no-scrollbar py-1">
              <button 
                onClick={() => setActiveLeftTab('desc')}
                className={`flex items-center gap-1.5 px-inset-sm py-1 rounded-DEFAULT font-headline-sm text-headline-sm transition-colors cursor-pointer ${activeLeftTab === 'desc' ? 'bg-surface-container text-primary' : 'text-on-surface-variant hover:text-on-surface hover:bg-surface-container'}`}
              >
                <span className="material-symbols-outlined text-[15px]">description</span>
                <span>Description</span>
                <span className="px-1.5 py-0.5 rounded-full bg-primary/10 text-primary font-label-sm text-label-sm leading-none">Specs</span>
              </button>
              <button 
                onClick={() => setActiveLeftTab('hints')}
                className={`flex items-center gap-1.5 px-inset-sm py-1 rounded-DEFAULT font-headline-sm text-headline-sm transition-colors cursor-pointer ${activeLeftTab === 'hints' ? 'bg-surface-container text-primary' : 'text-on-surface-variant hover:text-on-surface hover:bg-surface-container'}`}
              >
                <span className="material-symbols-outlined text-[15px]">lightbulb</span>
                <span>Hints</span>
                <span className="px-1.5 py-0.5 rounded-full bg-surface-container-high text-tertiary font-label-sm text-label-sm leading-none">3</span>
              </button>
              <button 
                onClick={() => setActiveLeftTab('subs')}
                className={`flex items-center gap-1.5 px-inset-sm py-1 rounded-DEFAULT font-headline-sm text-headline-sm transition-colors cursor-pointer ${activeLeftTab === 'subs' ? 'bg-surface-container text-primary' : 'text-on-surface-variant hover:text-on-surface hover:bg-surface-container'}`}
              >
                <span className="material-symbols-outlined text-[15px]">history</span>
                <span>Submissions</span>
                <span className="px-1.5 py-0.5 rounded-full bg-surface-container-high text-on-surface-variant font-label-sm text-label-sm leading-none">1.8k</span>
              </button>
            </div>
          </div>
          
          {/* Left Tab Views Container */}
          <div className="flex-1 overflow-y-auto px-inset-lg py-inset-lg select-text text-on-surface space-y-gutter-spacious">
            
            {activeLeftTab === 'desc' && (
              <div className="space-y-gutter-normal">
                {/* Meta Info & Title */}
                <div>
                  <h1 className="font-headline-lg text-headline-lg text-on-surface tracking-tight font-bold mb-4">#{problem.id}. {problem.title}</h1>
                </div>

                {/* Problem Narrative */}
                <div className="space-y-inset-md font-body-md text-body-md text-on-surface leading-relaxed whitespace-pre-line">
                  {problem.statement}
                </div>

                {/* Functional Requirements */}
                <div className="bg-surface-container p-inset-lg rounded-lg shadow-sm space-y-inset-sm">
                  <h2 className="font-headline-md text-headline-md text-on-surface flex items-center gap-inset-xs mb-3">
                    <span className="material-symbols-outlined text-primary text-[20px]">fact_check</span>
                    <span>Functional Requirements</span>
                  </h2>
                  <ul className="space-y-inset-xs font-body-md text-body-md text-on-surface-variant list-disc pl-5">
                    {problem.requirements.split('. ').map((req, i) => req && <li key={i}><strong className="text-on-surface font-medium">{req.trim()}</strong></li>)}
                  </ul>
                </div>

                {/* Non-Functional Constraints */}
                <div className="bg-surface-container p-inset-lg rounded-lg shadow-sm space-y-inset-sm">
                  <h2 className="font-headline-md text-headline-md text-on-surface flex items-center gap-inset-xs mb-3">
                    <span className="material-symbols-outlined text-secondary text-[20px]">tune</span>
                    <span>Non-Functional Requirements & Constraints</span>
                  </h2>
                  <ul className="space-y-inset-xs font-body-md text-body-md text-on-surface-variant list-disc pl-5">
                    {problem.assumptions.split('. ').map((req, i) => req && <li key={i}><span className="text-on-surface-variant">{req.trim()}</span></li>)}
                  </ul>
                </div>

              </div>
            )}
            
            {activeLeftTab === 'hints' && (
              <div className="space-y-gutter-normal">
                <div className="bg-surface-container p-inset-lg rounded-lg shadow-sm">
                  <span className="font-label-sm text-label-sm uppercase font-semibold text-tertiary">Hint 1</span>
                  <h3 className="font-headline-sm text-headline-sm text-on-surface mt-1">Vehicle vs Spot Granularity</h3>
                  <p className="font-body-md text-body-md text-on-surface-variant mt-inset-xs leading-relaxed">
                    Do not tightly couple a Vehicle class to the internal coordinates of a ParkingSpot. A Vehicle has an identity and size classification. The spot simply hosts a vehicle reference.
                  </p>
                </div>
                <div className="bg-surface-container p-inset-lg rounded-lg shadow-sm">
                  <span className="font-label-sm text-label-sm uppercase font-semibold text-tertiary">Hint 2</span>
                  <h3 className="font-headline-sm text-headline-sm text-on-surface mt-1">Locking Strategy</h3>
                  <p className="font-body-md text-body-md text-on-surface-variant mt-inset-xs leading-relaxed">
                    Avoid synchronizing on the whole ParkingLot method. Instead, hold atomic locks per Floor or use concurrent data structures.
                  </p>
                </div>
              </div>
            )}
            
            {activeLeftTab === 'subs' && (
              <div className="space-y-gutter-normal">
                <div className="bg-surface-container p-inset-lg rounded-lg shadow-sm">
                  <h3 className="font-headline-sm text-headline-sm text-on-surface mb-inset-md">Your Recent Submissions</h3>
                  <div className="text-on-surface-variant font-code-sm text-code-sm text-center py-4">No submissions yet for this problem.</div>
                </div>
              </div>
            )}

          </div>
        </section>

        {/* DRAGGABLE DIVIDER */}
        <div className="hidden lg:flex w-1.5 hover:w-2 bg-surface-container hover:bg-primary transition-all cursor-col-resize items-center justify-center select-none z-20 group" id="drag-handle">
          <div className="w-0.5 h-8 rounded-full bg-outline group-hover:bg-surface-container-lowest"></div>
        </div>

        {/* RIGHT PANE: Plain Text Submission Area */}
        <section className="w-full lg:flex-1 flex flex-col h-full bg-surface-container-lowest overflow-hidden">
          
          <div className="flex items-center justify-between px-inset-md bg-surface-container shadow-sm h-10 flex-shrink-0">
            <span className="font-code-sm text-code-sm text-on-surface-variant">Your Design Submission</span>
          </div>

          {/* Plain Text Canvas Area */}
          <div className="flex-1 flex overflow-hidden bg-surface-container-lowest text-on-surface font-code-md text-code-md leading-relaxed select-text relative">
            
            {/* Editor Textarea */}
            <textarea
              className="flex-1 overflow-y-auto overflow-x-auto p-inset-lg font-code-md text-code-md bg-transparent border-none text-on-surface resize-none focus:outline-none"
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder="Describe your classes, responsibilities, relationships..."
              spellCheck="false"
            />
          </div>
          
          {/* Sticky IDE Action Footer */}
          <footer className="h-12 bg-surface-container-high px-inset-md flex items-center justify-between shadow-lg flex-shrink-0 z-10 border-t border-border-default">
            
            <div className="flex items-center gap-inset-md">
              <div className="hidden sm:flex items-center gap-1.5 font-code-sm text-code-sm text-on-surface-variant">
                <span className="material-symbols-outlined text-[14px]">cloud_done</span>
                <span>Saved to workspace</span>
              </div>
            </div>

            {/* Error Message */}
            {error && <span className="text-error text-sm font-semibold mx-4">{error}</span>}

            {/* Right Footer Actions */}
            <div className="flex items-center gap-inset-sm">
              <button 
                className="px-inset-lg py-1.5 rounded bg-primary hover:bg-[#2cbb5d] text-surface-container-lowest font-headline-sm text-headline-sm font-bold transition-all disabled:opacity-50 flex items-center gap-2"
                onClick={handleSubmit} 
                disabled={submitting}
              >
                {submitting ? 'Submitting...' : (
                  <>
                    <span className="material-symbols-outlined text-[18px]">cloud_upload</span>
                    Submit Design
                  </>
                )}
              </button>
            </div>
          </footer>
        </section>
      </div>
    </div>
  );
}

export default ProblemDetail;
