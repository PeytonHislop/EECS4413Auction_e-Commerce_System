import { useEffect, useState } from "react";
import ModuleHeader from "../../../shared/components/ModuleHeader";
import StatusBanner from "../../../shared/components/StatusBanner";
import { leaderboardApi } from "../api/leaderboardApi";
import LeaderboardEntryCard from "../components/LeaderboardEntryCard";

export default function LeaderboardPage() {
  const [leaderboardEntries, setLeaderboardEntries] = useState([]);
  const [weeklyStats, setWeeklyStats] = useState(null);
  const [bidderId, setBidderId] = useState("");
  const [bidderStats, setBidderStats] = useState([]);
  const [error, setError] = useState("");

  const loadLeaderboard = async () => {
    try {
      setError("");
      const data = await leaderboardApi.getWeeklyLeaderboard();
      setLeaderboardEntries(data?.entries || []);
    } catch (err) {
      setError(err.message);
    }
  };

  const loadWeeklyStats = async () => {
    try {
      setError("");
      const stats = await leaderboardApi.getWeeklyStats();
      setWeeklyStats(stats);
    } catch (err) {
      setError(err.message);
    }
  };

  const loadBidderStats = async () => {
    if (!bidderId) {
      setBidderStats([]);
      return;
    }
    try {
      setError("");
      const stats = await leaderboardApi.getBidderStats(bidderId);
      setBidderStats(stats?.bids || []);
    } catch (err) {
      setError(err.message);
      setBidderStats([]);
    }
  };

  useEffect(() => {
    loadLeaderboard();
    loadWeeklyStats();
  }, []);

  return (
    <div className="page">
      <ModuleHeader
        title="Leaderboard"
        description="Leaderboard service owner: weekly top bidders and stats."
        owner="Leaderboard owner"
      />

      <StatusBanner
        error={error}
        notice={!error ? `Loaded ${leaderboardEntries.length} leaderboard entries` : ""}
      />

      <div className="card form-grid">
        <button className="btn" onClick={loadLeaderboard}>
          Refresh leaderboard
        </button>
        <button className="btn secondary" onClick={loadWeeklyStats}>
          Refresh stats
        </button>
      </div>

      {weeklyStats ? (
        <div className="card">
          <h4>Weekly Summary</h4>
          <div className="inline-meta">
            <span>Weekly bid count: {weeklyStats.weeklyBidCount}</span>
            <span>Top bids included: {weeklyStats.topBidsCount}</span>
            <span>Highest bid: {weeklyStats.highestBid ? `$${weeklyStats.highestBid}` : "n/a"}</span>
          </div>
          <div className="inline-meta">
            <span>Total value: {weeklyStats.totalBidValue ? `$${weeklyStats.totalBidValue}` : "n/a"}</span>
            <span>Average bid: {weeklyStats.averageBid ? `$${weeklyStats.averageBid}` : "n/a"}</span>
          </div>
        </div>
      ) : null}

      <div className="card form-grid">
        <label>
          Bidder ID
          <input value={bidderId} onChange={(e) => setBidderId(e.target.value)} />
        </label>
        <button className="btn secondary" onClick={loadBidderStats}>
          Load bidder stats
        </button>
      </div>

      {bidderStats.length ? (
        <div className="card">
          <h4>{bidderId} stats</h4>
          <div>Bid count: {bidderStats.length}</div>
        </div>
      ) : null}

      <div className="grid two">
        {leaderboardEntries.map((entry) => (
          <LeaderboardEntryCard key={`${entry.auctionId}-${entry.bidderId}-${entry.rank}`} entry={entry} />
        ))}
      </div>

      {!leaderboardEntries.length && !error ? (
        <div className="empty-state">No leaderboard entries are available yet.</div>
      ) : null}
    </div>
  );
}
