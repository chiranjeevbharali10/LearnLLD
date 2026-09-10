import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import ProblemList from './pages/ProblemList';
import ProblemDetail from './pages/ProblemDetail';
import AttemptResult from './pages/AttemptResult';
import AttemptHistory from './pages/AttemptHistory';

function App() {
  return (
    <BrowserRouter>
      <div className="bg-background font-body-md text-body-md text-on-surface antialiased selection:bg-primary/20 selection:text-primary min-h-screen flex flex-col">
        <header className="fixed top-0 left-0 right-0 z-50 bg-surface-container-lowest/95 backdrop-blur-md shadow-[0_1px_8px_rgba(0,0,0,0.04)]">
          <div className="h-14 w-full px-gutter-normal flex items-center justify-between gap-gutter-compact">
            <div className="flex items-center gap-gutter-compact min-w-0 flex-shrink-0">
              <Link to="/" className="flex items-center gap-inset-sm flex-shrink-0">
                <span className="material-symbols-outlined text-primary text-[24px]">terminal</span>
                <span className="font-headline-sm text-headline-sm font-semibold tracking-tight text-on-surface">LLD Practice</span>
              </Link>
              <div className="h-4 w-[1px] bg-surface-container-highest mx-inset-xs hidden sm:block"></div>
              <div className="hidden xl:flex items-center gap-inset-xs bg-surface-container-low px-inset-sm py-inset-xs rounded-lg">
                <span className="font-body-sm text-body-sm text-on-surface-variant">Workspace</span>
              </div>
            </div>
            
            <nav className="hidden lg:flex items-center gap-inset-xs">
              <Link to="/" className="px-inset-sm py-1.5 rounded-lg font-body-sm text-body-sm text-on-surface-variant hover:bg-surface-container hover:text-on-surface transition-colors">Problems</Link>
              <Link to="/history" className="px-inset-sm py-1.5 rounded-lg font-body-sm text-body-sm text-on-surface-variant hover:bg-surface-container hover:text-on-surface transition-colors">My History</Link>
            </nav>
            
            <div className="flex items-center gap-inset-sm flex-shrink-0">
              <button className="p-1.5 text-on-surface-variant hover:text-on-surface hover:bg-surface-container rounded-lg transition-colors" title="Settings">
                <span className="material-symbols-outlined text-[18px] block">settings</span>
              </button>
              <div className="flex items-center pl-inset-xs">
                <div className="w-8 h-8 rounded-full bg-surface-container-high flex items-center justify-center text-on-surface font-semibold text-sm ring-1 ring-outline-variant hover:ring-primary transition-all cursor-pointer">
                  U
                </div>
              </div>
            </div>
          </div>
        </header>
        
        <main className="w-full pt-14 bg-background flex-1 flex flex-col h-screen overflow-hidden">
          <Routes>
            <Route path="/" element={<ProblemList />} />
            <Route path="/problems/:id" element={<ProblemDetail />} />
            <Route path="/attempts/:id" element={<AttemptResult />} />
            <Route path="/history" element={<AttemptHistory />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App;
