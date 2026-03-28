import { useEffect, useState } from "react";
import ModuleHeader from "../../../shared/components/ModuleHeader";
import StatusBanner from "../../../shared/components/StatusBanner";
import JsonViewer from "../../../shared/components/JsonViewer";
import { useAuth } from "../../../shared/auth/AuthContext";
import { auctionApi } from "../api/auctionApi";
import AuctionForm from "../components/AuctionForm";

export default function CreateAuctionPage() {
  const { token, userId, role } = useAuth();
  const [form, setForm] = useState({
    itemId: "",
    sellerId: userId || "",
    durationHours: "",
    reservePrice: ""
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [response, setResponse] = useState(null);

  useEffect(() => {
    setForm((prev) => ({ ...prev, sellerId: userId || prev.sellerId }));
  }, [userId]);

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError("");
    setSuccess("");

    try {
      const payload = {
        itemId: form.itemId,
        sellerId: form.sellerId,
        durationHours: Number(form.durationHours),
        reservePrice: Number(form.reservePrice)
      };

      const data = await auctionApi.createAuction(payload, token);
      setResponse(data);
      setSuccess("Auction created successfully.");
      setForm((prev) => ({
        ...prev,
        itemId: "",
        durationHours: "",
        reservePrice: ""
      }));
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page">
      <ModuleHeader
        title="Create auction"
        description="Auction owner owns seller auction creation. The frontend includes sellerId to satisfy current request validation, even though the service also derives seller info from the token."
        owner="Auction owner"
      />

      <div className="notice">Current role: {role || "Unknown"}</div>
      <StatusBanner error={error} success={success} />
      <AuctionForm form={form} setForm={setForm} onSubmit={handleSubmit} loading={loading} />
      {response && <JsonViewer title="Create auction response" data={response} />}
    </div>
  );
}
