import { api } from "../../../shared/api/apiClient";

export const iamApi = {
  signup: (payload) => api.post("/api/auth/signup", payload),
  login: (payload) => api.post("/api/auth/login", payload),
  validate: (token) => api.post("/api/auth/validate", null, token),
  authorize: (token, requiredRole) =>
    api.get("/api/auth/authorize", token, { requiredRole }),
  forgotPassword: (payload) => api.post("/api/auth/password/forgot", payload),
  resetPassword: (payload) => api.post("/api/auth/password/reset", payload),
  getUserProfile: (userId, token) => api.get(`/api/users/${userId}`, token)
};
