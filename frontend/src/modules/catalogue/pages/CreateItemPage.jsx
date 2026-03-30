import { useState } from "react";
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
      const payload = {
        ...form,
        startPrice: Number(form.startPrice),
        shippingPrice: Number(form.shippingPrice),
        durationHours: Number(form.durationHours),
        auctionType: form.auctionType
      };

      const data = await catalogueApi.createItem(payload, token);
      setResponse(data);
      setSuccess("Item created successfully.");
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
        title="Create item"
        description="Catalogue owner owns seller item creation. Gateway enforces that only SELLER or ADMIN users should do this."
        owner="Catalogue owner"
      />

      <div className="notice">Current role: {role || "Unknown"}</div>
      <StatusBanner error={error} success={success} />
      <ItemForm form={form} setForm={setForm} onSubmit={handleSubmit} loading={loading} />
      {response && <JsonViewer title="Create item response" data={response} />}
      <div style={{ marginTop: "1rem" }}>
        <a className="btn secondary" href="/catalogue">Back to catalogue</a>
      </div>
    </div>
  );
}
