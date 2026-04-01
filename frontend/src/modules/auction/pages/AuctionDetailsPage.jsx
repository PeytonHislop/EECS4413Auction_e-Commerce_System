import { useEffect, useRef, useState } from "react";
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
  const [liveFlash, setLiveFlash] = useState(false);
  const [bidForm, setBidForm] = useState({
    bidderId: userId || "",
    bidAmount: ""
  });
  const [placingBid, setPlacingBid] = useState(false);

  useEffect(() => {
    setBidForm((prev) => ({ ...prev, bidderId: userId || prev.bidderId }));
  }, [userId]);

  // ── Initial data load ─────────────────────────────────────────────────────
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

  async function handleLoadBids() {
    try {
      setError("");
      const bidHistory = await auctionApi.getBidHistory(auctionId);
      setBids(Array.isArray(bidHistory) ? bidHistory : []);
    } catch (err) {
      setError(err.message);
    }
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
    return () => { ignore = true; };
  }, [auctionId]);

  // ── WebSocket — live bid updates ──────────────────────────────────────────
  useEffect(() => {
    if (!auctionId) return;

    // The callback that handles an incoming bid update
    function onBidUpdate(bidUpdate) {
      console.log("Live bid update received:", bidUpdate);

      // Update the auction card (current highest bid + bidder)
      setAuction((prev) =>
        prev
          ? {
              ...prev,
              currentHighestBid: bidUpdate.bidAmount,
              currentHighestBidderId: bidUpdate.bidderId
            }
          : prev
      );

      // Update bid summary card
      setHighestBid((prev) => ({
        ...(prev || {}),
        bidId: bidUpdate.bidId,
        auctionId: bidUpdate.auctionId,
        bidderId: bidUpdate.bidderId,
        bidAmount: bidUpdate.bidAmount,
        bidTimestamp: bidUpdate.bidTimestamp
      }));

      // Increment bid count immediately
      setBidCount((prev) =>
        prev ? { ...prev, bidCount: (prev.bidCount ?? 0) + 1 } : { bidCount: 1 }
      );

      // Prepend new bid to history table
      setBids((prev) => [
        {
          bidId: bidUpdate.bidId,
          auctionId: bidUpdate.auctionId,
          bidderId: bidUpdate.bidderId,
          bidAmount: bidUpdate.bidAmount,
          bidTimestamp: bidUpdate.bidTimestamp
        },
        ...prev
      ]);

      // Green flash on the bid summary card
      setLiveFlash(true);
      setTimeout(() => setLiveFlash(false), 1200);
    }

    // The wsClient is a singleton — it may already be connected from a
    // previous page visit. We handle both cases:
    //   A) Already connected → subscribe immediately
    //   B) Not yet connected → connect first, then subscribe in onConnect
    if (auctionApi.isWebSocketConnected()) {
      // Already connected — subscribe straight away
      auctionApi.subscribeToAuctionBids(auctionId, onBidUpdate);
    } else {
      // Not connected — connect then subscribe
      auctionApi.connectWebSocket(
        () => {
          console.log("WebSocket connected, subscribing to", auctionId);
          auctionApi.subscribeToAuctionBids(auctionId, onBidUpdate);
        },
        (err) => {
          console.warn("WebSocket unavailable, live updates disabled:", err?.message || err);
        }
      );
    }

    return () => {
      auctionApi.unsubscribeFromAuctionBids(auctionId);
    };
  }, [auctionId]);
  // ─────────────────────────────────────────────────────────────────────────

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
      // WebSocket handles the UI update — no need to reload
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
        description="Auction owner owns detail rendering, bid history, and bid submission UX. Bid updates are live via WebSocket."
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
            <div
              className="card"
              style={{
                transition: "box-shadow 0.3s ease",
                boxShadow: liveFlash ? "0 0 0 3px #22c55e" : undefined
              }}
            >
              <h3>
                Bid summary{" "}
                <span style={{
                  fontSize: "0.78rem",
                  background: "#dcfce7",
                  color: "#15803d",
                  borderRadius: "999px",
                  padding: "0.2rem 0.55rem",
                  marginLeft: "0.4rem"
                }}>
                  ● LIVE
                </span>
              </h3>
              <p className="metric">
                {formatCurrency(highestBid?.bidAmount || auction.currentHighestBid)}
              </p>
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
          <button className="btn secondary" onClick={handleLoadBids}>
              Load bid history
            </button>
          <BidHistoryTable bids={bids} />
          {highestBid && <JsonViewer title="Highest bid response" data={highestBid} />}
        </>
      ) : !error ? (
        <div className="empty-state">Loading auction details...</div>
      ) : null}
    </div>
  );
}
