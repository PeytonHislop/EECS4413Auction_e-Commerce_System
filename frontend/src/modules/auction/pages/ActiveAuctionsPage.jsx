import { useEffect, useState } from "react";
import ModuleHeader from "../../../shared/components/ModuleHeader";
import StatusBanner from "../../../shared/components/StatusBanner";
import { auctionApi } from "../api/auctionApi";
import AuctionCard from "../components/AuctionCard";

export default function ActiveAuctionsPage() {
  const [auctions, setAuctions] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    let ignore = false;

    async function load() {
      try {
        const data = await auctionApi.getActiveAuctions();
        if (!ignore) setAuctions(Array.isArray(data) ? data : []);
      } catch (err) {
        if (!ignore) setError(err.message);
      }
    }

    load();
    return () => {
      ignore = true;
    };
  }, []);

  return (
    <div className="page">
      <ModuleHeader
        title="Active auctions"
        description="Auction owner owns the public auction listing view."
        owner="Auction owner"
      />

      <StatusBanner error={error} notice={!error ? `Loaded ${auctions.length} active auction(s).` : ""} />

      <div className="grid two">
        {auctions.map((auction) => (
          <AuctionCard key={auction.auctionId} auction={auction} />
        ))}
      </div>

      {!auctions.length && !error ? (
        <div className="empty-state">No active auctions are currently available.</div>
      ) : null}
    </div>
  );
}
