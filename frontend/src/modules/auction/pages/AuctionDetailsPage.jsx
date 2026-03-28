import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import ModuleHeader from "../../../shared/components/ModuleHeader";
import StatusBanner from "../../../shared/components/StatusBanner";
import JsonViewer from "../../../shared/components/JsonViewer";
import { useAuth } from "../../../shared/auth/AuthContext";
import { formatCurrency, formatDate } from "../../../shared/utils/formatters";
import { auctionApi } from "../api/auctionApi";
import BidForm from "../components/BidForm";
import BidHistoryTable from "../components/BidHistoryTable";

export default function AuctionDetailsPage() {
  const { auctionId } = useParams();
  const { userId, token, role } = useAuth();

  const [auction, setAuction] = useState(null);
  const [bids, setBids] = useState([]);
  const [highestBid, setHighestBid] = useState(null);
  const [bidCount, setBidCount] = useState(null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [bidForm, setBidForm] = useState({
    bidderId: userId || "",
    bidAmount: ""
  });
  const [placingBid, setPlacingBid] = useState(false);

  useEffect(() => {
    setBidForm((prev) => ({ ...prev, bidderId: userId || prev.bidderId }));
  }, [userId]);

  async function loadAuction() {
    const [auctionData, bidHistory, highest, count] = await Promise.all([
      auctionApi.getAuctionById(auctionId),
      auctionApi.getBidHistory(auctionId).catch(() => []),
      auctionApi.getHighestBid(auctionId).catch(() => null),
      auctionApi.getBidCount(auctionId).catch(() => null)
    ]);

    setAuction(auctionData);
    setBids(Array.isArray(bidHistory) ? bidHistory : []);
    setHighestBid(highest);
    setBidCount(count);
  }

  useEffect(() => {
    let ignore = false;

    async function load() {
      try {
        await loadAuction();
      } catch (err) {
        if (!ignore) setError(err.message);
      }
    }

    load();
    return () => {
      ignore = true;
    };
  }, [auctionId]);

  async function handlePlaceBid(event) {
    event.preventDefault();
    setError("");
    setSuccess("");
    setPlacingBid(true);

    try {
      const payload = {
        bidderId: bidForm.bidderId,
        bidAmount: Number(bidForm.bidAmount)
      };
      const result = await auctionApi.placeBid(auctionId, payload, token);
      setSuccess(result.message || "Bid placed successfully.");
      setBidForm((prev) => ({ ...prev, bidAmount: "" }));
      await loadAuction();
    } catch (err) {
      setError(err.message);
    } finally {
      setPlacingBid(false);
    }
  }

  return (
    <div className="page">
      <ModuleHeader
        title={`Auction details · ${auctionId}`}
        description="Auction owner owns detail rendering, bid history, and bid submission UX."
        owner="Auction owner"
      />

      <StatusBanner error={error} success={success} />

      {auction ? (
        <>
          <div className="card">
            <h3>{auction.auctionId}</h3>
            <div className="inline-meta">
              <span>Item ID: {auction.itemId}</span>
              <span>Seller ID: {auction.sellerId}</span>
              <span>Status: {auction.status}</span>
              <span>Winner ID: {auction.winnerId || "—"}</span>
            </div>
            <div className="inline-meta" style={{ marginTop: "0.75rem" }}>
              <span>Reserve price: {formatCurrency(auction.reservePrice)}</span>
              <span>Current highest: {formatCurrency(auction.currentHighestBid)}</span>
              <span>Current highest bidder: {auction.currentHighestBidderId || "—"}</span>
              <span>Ends: {formatDate(auction.endTime)}</span>
            </div>
            <div className="card-actions" style={{ marginTop: "1rem" }}>
              <Link className="btn secondary" to="/payments/checkout">
                Go to checkout
              </Link>
              <Link className="btn secondary" to="/auctions">
                Back to auctions
              </Link>
            </div>
          </div>

          <div className="grid two">
            <div className="card">
              <h3>Bid summary</h3>
              <p className="metric">{formatCurrency(highestBid?.bidAmount || auction.currentHighestBid)}</p>
              <p>Highest bid</p>
              <div className="inline-meta">
                <span>Bid count: {bidCount?.bidCount ?? bids.length}</span>
                <span>Current role: {role || "Guest"}</span>
              </div>
            </div>

            <div className="card">
              <h3>Place a bid</h3>
              {role === "BUYER" || role === "ADMIN" ? (
                <BidForm
                  form={bidForm}
                  setForm={setBidForm}
                  onSubmit={handlePlaceBid}
                  loading={placingBid}
                />
              ) : (
                <div className="notice">Login as a BUYER or ADMIN to place a bid.</div>
              )}
            </div>
          </div>

          <BidHistoryTable bids={bids} />
          {highestBid && <JsonViewer title="Highest bid response" data={highestBid} />}
        </>
      ) : !error ? (
        <div className="empty-state">Loading auction details...</div>
      ) : null}
    </div>
  );
}
