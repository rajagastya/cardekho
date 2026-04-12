const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {})
    },
    ...options
  });

  if (!response.ok) {
    throw new Error(`Request failed: ${response.status}`);
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

export const api = {
  getMeta: () => request("/meta"),
  parseIntent: (payload) =>
    request("/ai/intake", {
      method: "POST",
      body: JSON.stringify(payload)
    }),
  getRecommendations: (payload) =>
    request("/recommendations", {
      method: "POST",
      body: JSON.stringify(payload)
    }),
  getShortlists: () => request("/shortlists"),
  saveShortlist: (payload) =>
    request("/shortlists", {
      method: "POST",
      body: JSON.stringify(payload)
    }),
  deleteShortlist: (id) =>
    request(`/shortlists/${id}`, {
      method: "DELETE"
    })
};
