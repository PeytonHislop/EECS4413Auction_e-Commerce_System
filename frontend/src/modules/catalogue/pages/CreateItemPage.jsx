import { useState } from "react";
import { useNavigate } from "react-router-dom";
import ModuleHeader from "../../../shared/components/ModuleHeader";
import StatusBanner from "../../../shared/components/StatusBanner";
import JsonViewer from "../../../shared/components/JsonViewer";
import { useAuth } from "../../../shared/auth/AuthContext";
import { catalogueApi } from "../api/catalogueApi";
import ItemForm from "../components/ItemForm";

const initialForm = {
  name: "",
  description: "",
  startPrice: "",
  shippingPrice: "",
  durationHours: "",
  auctionType: ""
};

export default function CreateItemPage() {
  const { token, role } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState(initialForm);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [response, setResponse] = useState(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError("");
    setSuccess("");

    try {
      // UC-CAT-7: Validate and create item with calculated end date
      const payload = {
        ...form,
        startPrice: Number(form.startPrice),
        shippingPrice: Number(form.shippingPrice),
        durationHours: Number(form.durationHours),
        auctionType: form.auctionType
      };

      const data = await catalogueApi.createItem(payload, token);
      setResponse(data);
      setSuccess("✓ Item listed successfully! Redirecting to catalogue...");
      setForm(initialForm);
      
      // Redirect after short delay
      setTimeout(() => navigate('/catalogue'), 2000);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page">
      <ModuleHeader
        title="List Auction Item"
        description="UC-CAT-7: Create a new item for auction with description, type, duration, and shipping"
        owner="Catalogue Service"
      />

      <div className="info-banner">
        <svg className="info-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <div>
          <strong>Current Role:</strong> {role || "Unknown"}
          <p style={{ margin: "0.25rem 0 0 0", fontSize: "0.9em", color: "#666" }}>Only SELLER or ADMIN roles can list items for auction</p>
        </div>
      </div>
      
      <StatusBanner error={error} success={success} />
      <ItemForm form={form} setForm={setForm} onSubmit={handleSubmit} loading={loading} />
      {response && <JsonViewer title="Item Creation Response (HATEOAS)" data={response} />}
      <div style={{ marginTop: "1rem" }}>
        <button className="btn secondary" onClick={() => navigate('/catalogue')}>
          <svg className="btn-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          Back to Catalogue
        </button>
      </div>
    </div>
  );
}
