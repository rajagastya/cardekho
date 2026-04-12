import { NavLink, Route, Routes } from "react-router-dom";
import HomePage from "./pages/HomePage";
import SavedShortlistsPage from "./pages/SavedShortlistsPage";

export default function App() {
  return (
    <div className="shell">
      <header className="hero">
        <div>
          <p className="eyebrow">AI-native car buying MVP</p>
          <h1>From car confusion to a shortlist you can actually trust.</h1>
          <p className="subtle">
            Tell us your budget, usage, and priorities. We score the catalog,
            explain trade-offs, and let you save a shortlist.
          </p>
        </div>
        <nav className="nav">
          <NavLink to="/" end>
            Finder
          </NavLink>
          <NavLink to="/saved">Saved shortlists</NavLink>
        </nav>
      </header>

      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/saved" element={<SavedShortlistsPage />} />
      </Routes>
    </div>
  );
}
