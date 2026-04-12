import { useEffect, useState } from "react";
import { api } from "../api/client";
import PreferenceForm from "../components/PreferenceForm";
import RecommendationPanel from "../components/RecommendationPanel";

export default function HomePage() {
  const [meta, setMeta] = useState({
    bodyStyles: ["Any"],
    fuelTypes: ["Any"],
    transmissions: ["Any"],
    priorities: ["Safety", "Mileage", "Features"],
    drivingMixes: ["City", "Highway", "Mixed"]
  });
  const [formState, setFormState] = useState();
  const [recommendation, setRecommendation] = useState(null);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [intentLoading, setIntentLoading] = useState(false);
  const [intentText, setIntentText] = useState(
    "I drive mostly in Bangalore traffic, budget around 15 lakh, want an automatic SUV for my family, parents will sit in the rear, and safety matters most."
  );
  const [intentMeta, setIntentMeta] = useState(null);
  const [message, setMessage] = useState("");

  useEffect(() => {
    api.getMeta().then(setMeta).catch(() => {
      setMessage("Backend metadata could not be loaded. Start Spring Boot on port 8080.");
    });
  }, []);

  async function handleSubmit(payload) {
    setLoading(true);
    setMessage("");
    try {
      const response = await api.getRecommendations({
        ...payload,
        primaryUse: payload.drivingMix.toLowerCase(),
        preferredTransmission: payload.preferredTransmission,
        fuelPreference: payload.fuelPreference,
        bodyStyle: payload.bodyStyle,
        priority: payload.priority,
        drivingMix: payload.drivingMix
      });
      setRecommendation(response);
    } catch (error) {
      setMessage("Could not fetch recommendations. Make sure the backend is running.");
    } finally {
      setLoading(false);
    }
  }

  async function handleIntentAutofill() {
    if (!intentText.trim()) {
      setMessage("Add a short buyer brief first so AI has something to interpret.");
      return;
    }

    setIntentLoading(true);
    setMessage("");
    try {
      const response = await api.parseIntent({ prompt: intentText });
      setFormState(response.preferences);
      setIntentMeta({
        source: response.source,
        signals: response.signals || []
      });
      setMessage("Buyer brief converted into structured preferences. You can edit before searching.");
    } catch (error) {
      setMessage("Could not parse the buyer brief right now.");
    } finally {
      setIntentLoading(false);
    }
  }

  async function handleSave() {
    if (!recommendation) {
      return;
    }

    setSaving(true);
    setMessage("");
    try {
      await api.saveShortlist({
        name: `${recommendation.recommendations[0].car.model} shortlist`,
        buyerSummary: recommendation.buyerSummary,
        cars: recommendation.recommendations.map((item) => item.car)
      });
      setMessage("Shortlist saved. You can view it on the Saved shortlists page.");
    } catch (error) {
      setMessage("Could not save shortlist right now.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <main className="content-grid">
      <PreferenceForm
        meta={meta}
        value={formState}
        onChange={setFormState}
        onSubmit={handleSubmit}
        loading={loading}
        intentText={intentText}
        onIntentTextChange={setIntentText}
        onIntentAutofill={handleIntentAutofill}
        intentLoading={intentLoading}
        intentMeta={intentMeta}
      />
      <RecommendationPanel data={recommendation} onSave={handleSave} saving={saving} />
      {message ? <p className="toast">{message}</p> : null}
    </main>
  );
}
