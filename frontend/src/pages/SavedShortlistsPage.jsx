import { useEffect, useState } from "react";
import { api } from "../api/client";

export default function SavedShortlistsPage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState("");

  async function loadShortlists() {
    setLoading(true);
    try {
      const response = await api.getShortlists();
      setItems(response);
    } catch (error) {
      setMessage("Could not load saved shortlists. Start the backend first.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadShortlists();
  }, []);

  async function handleDelete(id) {
    try {
      await api.deleteShortlist(id);
      setItems((current) => current.filter((item) => item.id !== id));
    } catch (error) {
      setMessage("Delete failed.");
    }
  }

  return (
    <main className="saved-layout">
      <section className="panel">
        <p className="section-tag">Step 3</p>
        <h2>Saved shortlists</h2>
        <p className="subtle">
          Keep a few recommendation sets around while you compare on-road price, test drive feel, and family comfort.
        </p>
      </section>

      {loading ? <p className="toast">Loading shortlists...</p> : null}
      {message ? <p className="toast">{message}</p> : null}

      <section className="card-grid">
        {items.map((item) => (
          <article key={item.id} className="panel shortlist-card">
            <div className="panel-title-row">
              <div>
                <p className="section-tag">Saved</p>
                <h3>{item.name}</h3>
              </div>
              <button className="ghost-button" onClick={() => handleDelete(item.id)}>
                Delete
              </button>
            </div>
            <p>{item.buyerSummary}</p>
            <div className="saved-chip-row">
              {item.cars.map((car) => (
                <span className="pill" key={car.id}>
                  {car.make} {car.model}
                </span>
              ))}
            </div>
          </article>
        ))}
      </section>
    </main>
  );
}
