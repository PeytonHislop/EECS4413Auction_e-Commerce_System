const DEFAULT_HEADERS = {
  Accept: "application/json"
};

async function parseResponse(response) {
  const text = await response.text();
  let data = text;

  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = text;
  }

  if (!response.ok) {
    const message =
      typeof data === "object" && data !== null
        ? data.message || data.error || JSON.stringify(data)
        : data || `Request failed with status ${response.status}`;

    throw new Error(message);
  }

  return data;
}

function buildUrl(path, query) {
  const url = new URL(path, window.location.origin);
  if (query) {
    Object.entries(query).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== "") {
        url.searchParams.set(key, value);
      }
    });
  }
  return url.pathname + url.search;
}

export async function apiRequest(path, options = {}) {
  const { method = "GET", body, token, query } = options;

  const headers = {
    ...DEFAULT_HEADERS,
    ...(body ? { "Content-Type": "application/json" } : {}),
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...(options.headers || {})
  };

  const response = await fetch(buildUrl(path, query), {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined
  });

  return parseResponse(response);
}

export const api = {
  get: (path, token, query) => apiRequest(path, { method: "GET", token, query }),
  post: (path, body, token, headers) =>
    apiRequest(path, { method: "POST", body, token, headers }),
  put: (path, body, token, headers) =>
    apiRequest(path, { method: "PUT", body, token, headers }),
  delete: (path, token) => apiRequest(path, { method: "DELETE", token })
};
