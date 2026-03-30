import { api } from "../../../shared/api/apiClient";

// Helper to extract items from HATEOAS response
function extractItems(response) {
  // HATEOAS format: { _embedded: { itemList: [...] }, _links: {...} }
  if (response?._embedded?.itemList) {
    return response._embedded.itemList;
  }
  // Fallback for plain array response
  return Array.isArray(response) ? response : [];
}

// Helper to extract single item from HATEOAS response
function extractItem(response) {
  // Single item with _links is already in the right format
  return response;
}

export const catalogueApi = {
  getItems: async () => {
    const response = await api.get("/api/catalogue/items");
    return extractItems(response);
  },
  getItemById: async (itemId) => {
    const response = await api.get(`/api/catalogue/items/${itemId}`);
    return extractItem(response);
  },
  createItem: (payload, token) => api.post("/api/catalogue/items", payload, token)
};
