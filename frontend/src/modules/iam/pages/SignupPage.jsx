import { useState } from "react";
import ModuleHeader from "../../../shared/components/ModuleHeader";
import StatusBanner from "../../../shared/components/StatusBanner";
import JsonViewer from "../../../shared/components/JsonViewer";
import { iamApi } from "../api/iamApi";

const initialForm = {
  username: "",
  password: "",
  firstName: "",
  lastName: "",
  email: "",
  role: "BUYER",
  shippingAddress: {
    streetNumber: "",
    streetName: "",
    city: "",
    country: "",
    postalCode: ""
  }
};

export default function SignupPage() {
  const [form, setForm] = useState(initialForm);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [response, setResponse] = useState(null);

  function updateAddress(field, value) {
    setForm((prev) => ({
      ...prev,
      shippingAddress: { ...prev.shippingAddress, [field]: value }
    }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError("");
    setSuccess("");
    setResponse(null);

    try {
      const data = await iamApi.signup(form);
      setResponse(data);
      setSuccess("Signup successful.");
      setForm(initialForm);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page">
      <ModuleHeader
        title="Signup"
        description="IAM owner owns user registration and shipping address capture here."
        owner="IAM owner"
      />

      <form className="card form-grid" onSubmit={handleSubmit}>
        <div className="form-grid two">
          <label>
            Username
            <input
              value={form.username}
              onChange={(e) => setForm((prev) => ({ ...prev, username: e.target.value }))}
            />
          </label>
          <label>
            Password
            <input
              type="password"
              value={form.password}
              onChange={(e) => setForm((prev) => ({ ...prev, password: e.target.value }))}
            />
          </label>
          <label>
            First name
            <input
              value={form.firstName}
              onChange={(e) => setForm((prev) => ({ ...prev, firstName: e.target.value }))}
            />
          </label>
          <label>
            Last name
            <input
              value={form.lastName}
              onChange={(e) => setForm((prev) => ({ ...prev, lastName: e.target.value }))}
            />
          </label>
          <label>
            Email
            <input
              value={form.email}
              onChange={(e) => setForm((prev) => ({ ...prev, email: e.target.value }))}
            />
          </label>
          <label>
            Role
            <select
              value={form.role}
              onChange={(e) => setForm((prev) => ({ ...prev, role: e.target.value }))}
            >
              <option value="BUYER">BUYER</option>
              <option value="SELLER">SELLER</option>
              <option value="ADMIN">ADMIN</option>
            </select>
          </label>
        </div>

        <h3>Shipping address</h3>
        <div className="form-grid two">
          <label>
            Street number
            <input
              value={form.shippingAddress.streetNumber}
              onChange={(e) => updateAddress("streetNumber", e.target.value)}
            />
          </label>
          <label>
            Street name
            <input
              value={form.shippingAddress.streetName}
              onChange={(e) => updateAddress("streetName", e.target.value)}
            />
          </label>
          <label>
            City
            <input value={form.shippingAddress.city} onChange={(e) => updateAddress("city", e.target.value)} />
          </label>
          <label>
            Country
            <input
              value={form.shippingAddress.country}
              onChange={(e) => updateAddress("country", e.target.value)}
            />
          </label>
          <label>
            Postal code
            <input
              value={form.shippingAddress.postalCode}
              onChange={(e) => updateAddress("postalCode", e.target.value)}
            />
          </label>
        </div>

        <StatusBanner error={error} success={success} />
        <button className="btn" disabled={loading}>
          {loading ? "Creating user..." : "Create user"}
        </button>
      </form>

      {response && <JsonViewer title="Signup response" data={response} />}
    </div>
  );
}
