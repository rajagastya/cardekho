const initialState = {
  name: "Rahul",
  budgetLakh: 15,
  primaryUse: "mixed",
  preferredTransmission: "Automatic",
  fuelPreference: "Petrol",
  bodyStyle: "SUV",
  familySize: 4,
  priority: "Safety",
  drivingMix: "City",
  notes: "",
  mustHaveFeatures: []
};

export default function PreferenceForm({
  meta,
  value,
  onChange,
  onSubmit,
  loading,
  intentText,
  onIntentTextChange,
  onIntentAutofill,
  intentLoading,
  intentMeta
}) {
  const form = value || initialState;

  function updateField(key, fieldValue) {
    onChange({
      ...form,
      [key]: fieldValue
    });
  }

  function handleSubmit(event) {
    event.preventDefault();
    onSubmit(form);
  }

  return (
    <form className="panel form-grid" onSubmit={handleSubmit}>
      <div className="panel-title-row">
        <div>
          <p className="section-tag">Step 1</p>
          <h2>Buyer profile</h2>
        </div>
        <span className="pill">Simple quiz</span>
      </div>

      <label className="full">
        Describe your ideal car
        <textarea
          rows="4"
          value={intentText}
          onChange={(e) => onIntentTextChange(e.target.value)}
          placeholder="Example: I drive mostly in Bangalore traffic, budget around 15 lakh, want an automatic SUV, parents sit in the rear, safety matters most."
        />
      </label>

      <div className="full intent-row">
        <button className="secondary-button" type="button" onClick={onIntentAutofill} disabled={intentLoading}>
          {intentLoading ? "AI is reading your brief..." : "Autofill with AI"}
        </button>
        {intentMeta ? (
          <p className="subtle">
            Extracted via <strong>{intentMeta.source}</strong>: {intentMeta.signals.join(" • ")}
          </p>
        ) : null}
      </div>

      <label>
        Your name
        <input value={form.name} onChange={(e) => updateField("name", e.target.value)} required />
      </label>

      <label>
        Budget (Rs. lakh)
        <input
          type="number"
          min="4"
          max="50"
          value={form.budgetLakh}
          onChange={(e) => updateField("budgetLakh", Number(e.target.value))}
          required
        />
      </label>

      <label>
        Body style
        <select value={form.bodyStyle} onChange={(e) => updateField("bodyStyle", e.target.value)} required>
          {meta.bodyStyles.map((item) => (
            <option key={item} value={item}>
              {item}
            </option>
          ))}
        </select>
      </label>

      <label>
        Fuel preference
        <select value={form.fuelPreference} onChange={(e) => updateField("fuelPreference", e.target.value)} required>
          {meta.fuelTypes.map((item) => (
            <option key={item} value={item}>
              {item}
            </option>
          ))}
        </select>
      </label>

      <label>
        Transmission
        <select
          value={form.preferredTransmission}
          onChange={(e) => updateField("preferredTransmission", e.target.value)}
          required
        >
          {meta.transmissions.map((item) => (
            <option key={item} value={item}>
              {item}
            </option>
          ))}
        </select>
      </label>

      <label>
        Family size
        <input
          type="number"
          min="1"
          max="8"
          value={form.familySize}
          onChange={(e) => updateField("familySize", Number(e.target.value))}
          required
        />
      </label>

      <label>
        Top priority
        <select value={form.priority} onChange={(e) => updateField("priority", e.target.value)} required>
          {meta.priorities.map((item) => (
            <option key={item} value={item}>
              {item}
            </option>
          ))}
        </select>
      </label>

      <label>
        Driving mix
        <select value={form.drivingMix} onChange={(e) => updateField("drivingMix", e.target.value)} required>
          {meta.drivingMixes.map((item) => (
            <option key={item} value={item}>
              {item}
            </option>
          ))}
        </select>
      </label>

      <label className="full">
        Notes
        <textarea
          rows="4"
          value={form.notes}
          onChange={(e) => updateField("notes", e.target.value)}
          placeholder="Example: Mostly city use, parents sit in the rear, want lower maintenance."
        />
      </label>

      <button className="primary-button full" type="submit" disabled={loading}>
        {loading ? "Finding best matches..." : "Find my shortlist"}
      </button>
    </form>
  );
}
