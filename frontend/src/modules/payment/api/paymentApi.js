import { api } from "../../../shared/api/apiClient";

export const paymentApi = {
  processPayment: (payload, token) => api.post("/api/payments/process", payload, token)
};
