import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { iamApi } from "../api/iamApi";
import { useAuth } from "../../../shared/auth/AuthContext";
import ModuleHeader from "../../../shared/components/ModuleHeader";
import StatusBanner from "../../../shared/components/StatusBanner";

export default function LoginPage() {
  const navigate = useNavigate();
  const { loginFromResponse } = useAuth();

  const [form, setForm] = useState({ username: "", password: "" });
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError("");
    setSuccess("");

    try {
      const response = await iamApi.login(form);
      loginFromResponse(response);
      setSuccess("Login successful.");
      navigate("/iam/profile");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page">
      <ModuleHeader
        title="Login"
        description="IAM owner maintains this page. It calls the gateway login endpoint and stores the JWT in shared auth state."
        owner="IAM owner"
      />

      <div className="grid two">
        <form className="card form-grid" onSubmit={handleSubmit}>
          <label>
            Username
            <input
              value={form.username}
              onChange={(e) => setForm((prev) => ({ ...prev, username: e.target.value }))}
              placeholder="Enter username"
            />
          </label>

          <label>
            Password
            <input
              type="password"
              value={form.password}
              onChange={(e) => setForm((prev) => ({ ...prev, password: e.target.value }))}
              placeholder="Enter password"
            />
          </label>

          <StatusBanner error={error} success={success} />
          <button className="btn" disabled={loading}>
            {loading ? "Logging in..." : "Login"}
          </button>
        </form>

        <div className="card">
          <h3>What this page owns</h3>
          <ul>
            <li>POST <code>/api/auth/login</code></li>
            <li>Stores token, userId, username, and role in shared auth context</li>
            <li>Redirects into authenticated pages</li>
          </ul>
        </div>
      </div>
    </div>
  );
}
