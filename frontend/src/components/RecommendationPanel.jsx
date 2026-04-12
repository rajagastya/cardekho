export default function RecommendationPanel({ data, onSave, saving }) {
  if (!data) {
    return (
      <section className="panel placeholder-panel">
        <p className="section-tag">Step 2</p>
        <h2>Your shortlist will appear here</h2>
        <p className="subtle">
          We'll rank four to five cars, explain why they fit, and call out what to double-check before buying.
        </p>
      </section>
    );
  }

  return (
    <section className="results-stack">
      <article className="panel accent-panel">
        <p className="section-tag">Buyer readout</p>
        <h2>{data.buyerSummary}</h2>
        <p>{data.aiSummary}</p>
        <button className="secondary-button" onClick={onSave} disabled={saving}>
          {saving ? "Saving..." : "Save this shortlist"}
        </button>
      </article>

      <section className="card-grid">
        {data.recommendations.map((item, index) => (
          <article key={item.car.id} className="panel car-card">
            <img className="car-image" src={item.car.imageUrl} alt={`${item.car.make} ${item.car.model}`} />
            <div className="panel-title-row">
              <div>
                <p className="section-tag">#{index + 1} pick</p>
                <h3>
                  {item.car.make} {item.car.model}
                </h3>
              </div>
              <div className="score-stack">
                <span className="score-badge">{item.score}</span>
                <span className="confidence-badge">{item.confidenceScore}% fit</span>
              </div>
            </div>
            <p className="variant-line">
              {item.car.variant} | {item.car.bodyType} | {item.car.fuelType} | {item.car.transmission}
            </p>
            <p className="highlight">{item.car.highlight}</p>
            <div className="metric-row">
              <span className="pill">{item.car.userRating} rating</span>
              <span className="pill">{item.car.bootSpaceLiters}L boot</span>
              <span className="pill">{item.car.seatingCapacity} seats</span>
              <span className="pill">{item.car.reviewCount} reviews</span>
            </div>
            <ul className="reason-list">
              {item.reasons.map((reason) => (
                <li key={reason}>{reason}</li>
              ))}
            </ul>
            <details className="why-panel">
              <summary>Why this car?</summary>
              <p>{item.whyThisCarTip}</p>
            </details>
            <p className="caution">Watch-out: {item.caution}</p>
          </article>
        ))}
      </section>

      <article className="panel">
        <p className="section-tag">Quick compare</p>
        <div className="compare-table">
          <div className="compare-row compare-head">
            <span>Car</span>
            <span>Price</span>
            <span>Mileage</span>
            <span>Safety</span>
            <span>Boot</span>
            <span>Rating</span>
          </div>
          {data.recommendations.map((item) => (
            <div className="compare-row" key={item.car.id}>
              <span>{item.car.model}</span>
              <span>Rs. {item.car.priceLakh}L</span>
              <span>{item.car.mileage} kmpl</span>
              <span>{item.car.safetyRating}/5</span>
              <span>{item.car.bootSpaceLiters}L</span>
              <span>{item.car.userRating}</span>
            </div>
          ))}
        </div>
      </article>
    </section>
  );
}
