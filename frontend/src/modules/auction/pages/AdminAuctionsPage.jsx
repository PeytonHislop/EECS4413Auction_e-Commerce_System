import { useState } from "react";
import ModuleHeader from "../../../shared/components/ModuleHeader";
import StatusBanner from "../../../shared/components/StatusBanner";
import JsonViewer from "../../../shared/components/JsonViewer";
import { useAuth } from "../../../shared/auth/AuthContext";
import { auctionApi } from "../api/auctionApi";

export default function AdminAuctionsPage() {
  const { token } = useAuth();
  const [auctionId, setAuctionId] = useState("");
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  async function handleCloseAuction() {
    try {
      setError("");
      setSuccess("");
      const data = await auctionApi.closeAuction(auctionId, token);
      setResult(data);
      setSuccess("Auction close request completed.");
    } catch (err) {
      setError(err.message);
    }
  }

  async function handleCloseExpired() {
    try {
      setError("");
      setSuccess("");
      const data = await auctionApi.closeExpiredAuctions(token);
      setResult(data);
      setSuccess("Close-expired request completed.");
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="page">
      <ModuleHeader
        title="Auction admin tools"
        description="Auction owner owns manual admin close flows."
        owner="Auction owner"
      />

      <StatusBanner error={error} success={success} />

      <div className="grid two">
        <div className="card form-grid">
          <h3>Close a single auction</h3>
          <label>
            Auction ID
            <input value={auctionId} onChange={(e) => setAuctionId(e.target.value)} />
          </label>
          <button className="btn warning" onClick={handleCloseAuction}>
            Close auction
          </button>
        </div>

        <div className="card form-grid">
          <h3>Close expired auctions</h3>
          <p>Calls the gateway endpoint that forwards the admin request to the auction service.</p>
          <button className="btn danger" onClick={handleCloseExpired}>
            Close expired auctions
          </button>
        </div>
      </div>

      {result && <JsonViewer title="Admin response" data={result} />}
    </div>
  );
}
