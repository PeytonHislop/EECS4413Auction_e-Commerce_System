import { useEffect, useState } from "react";
import ModuleHeader from "../../../shared/components/ModuleHeader";
import StatusBanner from "../../../shared/components/StatusBanner";
import LeaderboardEntryCard from "../components/LeaderboardEntryCard";
import { formatCurrency } from "../../../shared/utils/formatters";
import { leaderboardApi } from "../api/leaderboardApi";

export default function LeaderboardPage() {
  const [leaderboardEntries, setLeaderboardEntries] = useState([]);
  const [weeklyStats, setWeeklyStats] = useState(null);
  const [bidderId, setBidderId] = useState("");
  const [bidderStats, setBidderStats] = useState(null);
  const [notice, setNotice] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  // Load leaderboard + stats together on mount
  async function loadAll() {
    setLoading(true);
    setError("");
    setNotice("");
    try {
      const [leaderboardData, statsData] = await Promise.all([
        leaderboardApi.getWeeklyLeaderboard(),
        leaderboardApi.getWeeklyStats()
      ]);
      setLeaderboardEntries(leaderboardData?.entries || []);
      setWeeklyStats(statsData);
      if (!(leaderboardData?.entries || []).length) {
        setNotice("No leaderboard entries yet this week. Place some bids to appear here.");
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  async function loadBidderStats() {
    if (!bidderId.trim()) return;
    setError("");
    setNotice("");
    try {
      const stats = await leaderboardApi.getBidderStats(bidderId.trim());
      setBidderStats(stats);
      if (!(stats?.bids || []).length) {
        setNotice(`No bids found for bidder "${bidderId}" this week.`);
      }
    } catch (err) {
      setError(err.message);
      setBidderStats(null);
    }
  }

  useEffect(() => {
    loadAll();
  }, []);

  return (
    <div className="page">
      <ModuleHeader
        title="Weekly Leaderboard"
        description="Top 10 highest bids this week. Resets every Monday at midnight. Auction owner owns this feature."
        owner="Auction owner"
      />

      <StatusBanner
        error={error}
        notice={!error && !loading ? notice || `Showing ${leaderboardEntries.length} entr${leaderboardEntries.length === 1 ? "y" : "ies"} for the current week.` : ""}
      />

      {/* ── Refresh buttons ─────────────────────────────────────────────── */}
      <div style={{ display: "flex", gap: "0.75rem" }}>
        <button className="btn" onClick={loadAll} disabled={loading}>
          {loading ? "Loading…" : "Refresh leaderboard"}
        </button>
      </div>

      {/* ── Weekly Summary ───────────────────────────────────────────────── */}
      {weeklyStats && (
        <div className="card">
          <h3>Weekly Summary</h3>
          <div className="grid three" style={{ gridTemplateColumns: "repeat(3, minmax(0, 1fr))" }}>
            <div>
              <p style={{ margin: "0 0 0.25rem", color: "#4b5563", fontSize: "0.9rem" }}>
                Highest bid
              </p>
              <p className="metric" style={{ margin: 0 }}>
                {weeklyStats.highestBid != null ? formatCurrency(weeklyStats.highestBid) : "—"}
              </p>
            </div>
            <div>
              <p style={{ margin: "0 0 0.25rem", color: "#4b5563", fontSize: "0.9rem" }}>
                Average bid
              </p>
              <p className="metric" style={{ margin: 0 }}>
                {weeklyStats.averageBid != null ? formatCurrency(weeklyStats.averageBid) : "—"}
              </p>
            </div>
            <div>
              <p style={{ margin: "0 0 0.25rem", color: "#4b5563", fontSize: "0.9rem" }}>
                Total bids this week
              </p>
              <p className="metric" style={{ margin: 0 }}>
                {weeklyStats.weeklyBidCount ?? 0}
              </p>
            </div>
          </div>
        </div>
      )}

      {/* ── Top 10 Leaderboard entries ───────────────────────────────────── */}
      {loading ? (
        <div className="empty-state">Loading leaderboard…</div>
      ) : leaderboardEntries.length > 0 ? (
        <div className="card">
          <h3>Top {leaderboardEntries.length} Bidders — Current Week</h3>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Rank</th>
                  <th>Bidder</th>
                  <th>Auction</th>
                  <th>Item</th>
                  <th>Bid Amount</th>
                  <th>Seller</th>
                </tr>
              </thead>
              <tbody>
                {leaderboardEntries.map((entry, index) => {
                  const rank = entry.rank ?? index + 1;
                  const medal = rank === 1 ? "🥇" : rank === 2 ? "🥈" : rank === 3 ? "🥉" : null;
                  return (
                    <tr key={`${entry.auctionId}-${entry.bidderId}-${rank}`}>
                      <td>
                        <strong>{medal ? `${medal} ${rank}` : `#${rank}`}</strong>
                      </td>
                      <td>{entry.bidderName || entry.bidderId}</td>
                      <td style={{ fontSize: "0.85rem", color: "#4b5563" }}>
                        {entry.auctionId}
                      </td>
                      <td>{entry.itemId}</td>
                      <td>
                        <strong>{formatCurrency(entry.bidAmount)}</strong>
                      </td>
                      <td>{entry.sellerName || entry.sellerId || "—"}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      ) : !error ? (
        <div className="empty-state">No leaderboard entries yet this week.</div>
      ) : null}

      {/* ── Bidder stats lookup ──────────────────────────────────────────── */}
      <div className="card form-grid">
        <h3>Look up a bidder</h3>
        <label>
          Bidder ID
          <input
            value={bidderId}
            onChange={(e) => setBidderId(e.target.value)}
            placeholder="e.g. a80f60ac-3559-4c5c-854d..."
            onKeyDown={(e) => e.key === "Enter" && loadBidderStats()}
          />
        </label>
        <button className="btn secondary" onClick={loadBidderStats}>
          Load bidder stats
        </button>
      </div>

      {bidderStats && (
        <div className="card">
          <h3>Stats for {bidderStats.bidderId}</h3>
          <div className="inline-meta">
            <span>Bids this week: <strong>{bidderStats.bidCount ?? 0}</strong></span>
            {bidderStats.highestBid != null && (
              <span>Highest bid: <strong>{formatCurrency(bidderStats.highestBid)}</strong></span>
            )}
          </div>
          {(bidderStats.bids || []).length > 0 && (
            <div className="table-wrap" style={{ marginTop: "0.75rem" }}>
              <table>
                <thead>
                  <tr>
                    <th>Auction</th>
                    <th>Item</th>
                    <th>Amount</th>
                  </tr>
                </thead>
                <tbody>
                  {bidderStats.bids.map((bid, i) => (
                    <tr key={i}>
                      <td style={{ fontSize: "0.85rem" }}>{bid.auctionId}</td>
                      <td>{bid.itemId}</td>
                      <td><strong>{formatCurrency(bid.bidAmount)}</strong></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
