import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { api } from "../api/apiClient";

const STORAGE_KEY = "code2cash-auth";
const AuthContext = createContext(null);

function loadStoredAuth() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(loadStoredAuth());

  useEffect(() => {
    if (auth) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(auth));
    } else {
      localStorage.removeItem(STORAGE_KEY);
    }
  }, [auth]);

  const value = useMemo(
    () => ({
      auth,
      isAuthenticated: Boolean(auth?.token),
      token: auth?.token || "",
      userId: auth?.userId || "",
      username: auth?.username || "",
      role: auth?.role || "",
      loginFromResponse(response) {
        const next = {
          token: response.token,
          tokenType: response.tokenType || "Bearer",
          expiresIn: response.expiresIn,
          userId: response.userId,
          username: response.username,
          role: response.role
        };
        setAuth(next);
        return next;
      },
      logout() {
        setAuth(null);
      },
      async validateCurrentToken() {
        if (!auth?.token) {
          return { valid: false };
        }
        return api.post("/api/auth/validate", null, auth.token);
      }
    }),
    [auth]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider");
  }
  return context;
}
