import { useState } from "react";
import ModuleHeader from "../../../shared/components/ModuleHeader";
import StatusBanner from "../../../shared/components/StatusBanner";
import JsonViewer from "../../../shared/components/JsonViewer";
import { useAuth } from "../../../shared/auth/AuthContext";
import { iamApi } from "../api/iamApi";

export default function AuthToolsPage() {
  const { token } = useAuth();

  const [requiredRole, setRequiredRole] = useState("BUYER");
  const [forgotUsername, setForgotUsername] = useState("");
  const [resetToken, setResetToken] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [error, setError] = useState("");
  const [result, setResult] = useState(null);
  const [success, setSuccess] = useState("");

  async function handleValidate() {
    try {
      setError("");
      setSuccess("");
      const data = await iamApi.validate(token);
      setResult(data);
    } catch (err) {
      setError(err.message);
    }
  }

  async function handleAuthorize() {
    try {
      setError("");
      setSuccess("");
      const data = await iamApi.authorize(token, requiredRole);
      setResult(data);
    } catch (err) {
      setError(err.message);
    }
  }

  async function handleForgot(event) {
    event.preventDefault();
    try {
      setError("");
      setSuccess("");
      const data = await iamApi.forgotPassword({ username: forgotUsername });
      setResult(data);
      setSuccess("Reset token generated.");
    } catch (err) {
      setError(err.message);
    }
  }

  async function handleReset(event) {
    event.preventDefault();
    try {
      setError("");
      setSuccess("");
      const data = await iamApi.resetPassword({ resetToken, newPassword });
      setResult(data);
      setSuccess("Password reset successful.");
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="page">
      <ModuleHeader
        title="IAM tools"
        description="This page lets the IAM owner and teammates test validate, authorize, forgot password, and reset password flows from the frontend."
        owner="IAM owner"
      />

      <StatusBanner error={error} success={success} />

      <div className="grid two">
        <div className="card">
          <h3>Token tools</h3>
          <div className="card-actions">
            <button className="btn" onClick={handleValidate}>
              Validate current token
            </button>
          </div>

          <div className="form-grid" style={{ marginTop: "1rem" }}>
            <label>
              Required role
              <select value={requiredRole} onChange={(e) => setRequiredRole(e.target.value)}>
                <option value="BUYER">BUYER</option>
                <option value="SELLER">SELLER</option>
                <option value="ADMIN">ADMIN</option>
              </select>
            </label>
            <button className="btn secondary" onClick={handleAuthorize}>
              Authorize role
            </button>
          </div>
        </div>

        <form className="card form-grid" onSubmit={handleForgot}>
          <h3>Forgot password</h3>
          <label>
            Username
            <input value={forgotUsername} onChange={(e) => setForgotUsername(e.target.value)} />
          </label>
          <button className="btn">Generate reset token</button>
        </form>
      </div>

      <form className="card form-grid" onSubmit={handleReset}>
        <h3>Reset password</h3>
        <div className="form-grid two">
          <label>
            Reset token
            <input value={resetToken} onChange={(e) => setResetToken(e.target.value)} />
          </label>
          <label>
            New password
            <input
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
            />
          </label>
        </div>
        <button className="btn">Reset password</button>
      </form>

      {result && <JsonViewer data={result} />}
    </div>
  );
}
