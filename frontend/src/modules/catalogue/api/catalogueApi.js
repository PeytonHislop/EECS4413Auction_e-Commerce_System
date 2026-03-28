import { api } from "../../../shared/api/apiClient";

export const catalogueApi = {
  getItems: () => api.get("/api/items"),
  getItemById: (itemId) => api.get(`/api/items/${itemId}`),
  createItem: (payload, token) => api.post("/api/items", payload, token)
};
