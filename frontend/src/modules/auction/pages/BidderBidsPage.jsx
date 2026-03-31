import { useEffect, useState } from "react";
import ModuleHeader from "../../../shared/components/ModuleHeader";
import StatusBanner from "../../../shared/components/StatusBanner";
import { useAuth } from "../../../shared/auth/AuthContext";
import { auctionApi } from "../api/auctionApi";
import BidHistoryTable from "../components/BidHistoryTable";

export default function BidderBidsPage() {
  const { userId } = useAuth();
  const [bidderId, setBidderId] = useState(userId || "");
  const [bids, setBids] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    setBidderId(userId || "");
  }, [userId]);

  async function handleLoad() {
    try {
      setError("");
      const data = await auctionApi.getBidsByBidder(bidderId);
      setBids(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message);
    }
  }

  useEffect(() => {
    if (bidderId) {
      handleLoad();
    }
  }, [bidderId]);

  return (
    <div className="page">
      <ModuleHeader
        title="My bids"
        description="Auction owner owns bidder history views."
        owner="Auction owner"
      />

      <div className="card form-grid">
        <label>
          Bidder ID
          <input value={bidderId} onChange={(e) => setBidderId(e.target.value)} />
        </label>
        <button className="btn secondary" onClick={handleLoad}>
          Load bids
        </button>
      </div>

      <StatusBanner error={error} notice={!error ? `Loaded ${bids.length} bid(s).` : ""} />
      <BidHistoryTable bids={bids} title="Bids by bidder" />
    </div>
  );
}
