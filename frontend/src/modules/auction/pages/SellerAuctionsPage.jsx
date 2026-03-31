import { useEffect, useState } from "react";
import ModuleHeader from "../../../shared/components/ModuleHeader";
import StatusBanner from "../../../shared/components/StatusBanner";
import { useAuth } from "../../../shared/auth/AuthContext";
import { auctionApi } from "../api/auctionApi";
import AuctionCard from "../components/AuctionCard";

export default function SellerAuctionsPage() {
  const { userId } = useAuth();
  const [auctions, setAuctions] = useState([]);
  const [sellerId, setSellerId] = useState(userId || "");
  const [error, setError] = useState("");

  useEffect(() => {
    setSellerId(userId || "");
  }, [userId]);

  async function handleLoad() {
    try {
      setError("");
      const data = await auctionApi.getSellerAuctions(sellerId);
      setAuctions(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message);
    }
  }

  useEffect(() => {
    if (sellerId) {
      handleLoad();
    }
  }, [sellerId]);

  return (
    <div className="page">
      <ModuleHeader
        title="Seller auctions"
        description="Auction owner owns seller-specific auction views."
        owner="Auction owner"
      />

      <div className="card form-grid">
        <label>
          Seller ID
          <input value={sellerId} onChange={(e) => setSellerId(e.target.value)} />
        </label>
        <button className="btn secondary" onClick={handleLoad}>
          Load seller auctions
        </button>
      </div>

      <StatusBanner error={error} notice={!error ? `Loaded ${auctions.length} auction(s).` : ""} />

      <div className="grid two">
        {auctions.map((auction) => (
          <AuctionCard key={auction.auctionId} auction={auction} />
        ))}
      </div>
    </div>
  );
}
