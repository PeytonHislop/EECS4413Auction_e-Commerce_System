import { useEffect, useState } from "react";
import ModuleHeader from "../../../shared/components/ModuleHeader";
import StatusBanner from "../../../shared/components/StatusBanner";
import { leaderboardApi } from "../api/leaderboardApi";
import LeaderboardEntryCard from "../components/LeaderboardEntryCard";
import { formatCurrency, formatDate } from "../../../shared/utils/formatters";

export default function LeaderboardPage() {
  const [leaderboardEntries, setLeaderboardEntries] = useState([]);
  const [weeklyStats, setWeeklyStats] = useState(null);
  const [bidderId, setBidderId] = useState("");
  const [bidderStats, setBidderStats] = useState([]);
  const [highestPeriod, setHighestPeriod] = useState("WEEK");
  const [highestByPeriod, setHighestByPeriod] = useState(null);
  const [topBidderPeriod, setTopBidderPeriod] = useState("WEEK");
  const [topBidderLimit, setTopBidderLimit] = useState(5);
  const [topBidders, setTopBidders] = useState([]);
  const [notice, setNotice] = useState("");
  const [error, setError] = useState("");

  const loadLeaderboard = async () => {
    try {
      setError("");
      setNotice("");
      const data = await leaderboardApi.getWeeklyLeaderboard();
      setLeaderboardEntries(data?.entries || []);
      if (!data?.entries?.length) {
        setNotice("No leaderboard entries yet. Place bids while leaderboard-service is running.");
      }
    } catch (err) {
      setError(err.message);
    }
  };

  const loadWeeklyStats = async () => {
    try {
      setError("");
      setNotice("");
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
      setNotice("");
      const stats = await leaderboardApi.getBidderStats(bidderId);
      setBidderStats(stats?.bids || []);
      if (!(stats?.bids || []).length) {
        setNotice("No bidder stats found for this bidder in the selected period.");
      }
    } catch (err) {
      setError(err.message);
      setBidderStats([]);
    }
  };

  const loadHighestByPeriod = async () => {
    try {
      setError("");
      setNotice("");
      const data = await leaderboardApi.getHighestByPeriod(highestPeriod);
      setHighestByPeriod(data);
      if (data && !data.hasEntry) {
        setNotice(`No highest bid entry found for ${highestPeriod}.`);
      }
    } catch (err) {
      setError(err.message);
      setHighestByPeriod(null);
    }
  };

  const loadTopBidders = async () => {
    if (!Number.isInteger(topBidderLimit) || topBidderLimit < 1 || topBidderLimit > 20) {
      setError("Top bidder limit must be an integer from 1 to 20.");
      return;
    }
    try {
      setError("");
      setNotice("");
      const data = await leaderboardApi.getTopBiddersByPeriod(topBidderPeriod, topBidderLimit);
      setTopBidders(data?.entries || []);
      if (!(data?.entries || []).length) {
        setNotice(`No top bidders found for ${topBidderPeriod}.`);
      }
    } catch (err) {
      setError(err.message);
      setTopBidders([]);
    }
  };

  useEffect(() => {
    loadLeaderboard();
    loadWeeklyStats();
    loadHighestByPeriod();
    loadTopBidders();
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
        notice={!error ? notice || `Loaded ${leaderboardEntries.length} leaderboard entries` : ""}
      />

      <div className="card form-grid">
        <button className="btn" onClick={loadLeaderboard}>
          Refresh leaderboard
        </button>
        <button className="btn secondary" onClick={loadWeeklyStats}>
          Refresh stats
        </button>
      </div>

      <div className="card form-grid">
        <label>
          Highest bid period
          <select value={highestPeriod} onChange={(e) => setHighestPeriod(e.target.value)}>
            <option value="DAY">Day</option>
            <option value="WEEK">Week</option>
            <option value="YEAR">Year</option>
          </select>
        </label>
        <button className="btn secondary" onClick={loadHighestByPeriod}>
          Load highest bid
        </button>
      </div>

      {highestByPeriod?.hasEntry && highestByPeriod?.entry ? (
        <div className="card">
          <h4>Highest bid ({highestByPeriod.period})</h4>
          <div className="inline-meta">
            <span>Bidder: {highestByPeriod.entry.bidderName || highestByPeriod.entry.bidderId}</span>
            <span>Auction: {highestByPeriod.entry.auctionId}</span>
          </div>
          <div className="inline-meta">
            <span>Amount: {formatCurrency(highestByPeriod.entry.bidAmount)}</span>
            <span>At: {formatDate(highestByPeriod.entry.bidTime)}</span>
          </div>
        </div>
      ) : highestByPeriod && !highestByPeriod.hasEntry ? (
        <div className="card">
          <h4>Highest bid ({highestByPeriod.period})</h4>
          <div>No bids found for this period yet.</div>
        </div>
      ) : null}

      <div className="card form-grid">
        <label>
          Top bidders period
          <select value={topBidderPeriod} onChange={(e) => setTopBidderPeriod(e.target.value)}>
            <option value="DAY">Day</option>
            <option value="WEEK">Week</option>
            <option value="YEAR">Year</option>
          </select>
        </label>
        <label>
          Limit
          <input
            type="number"
            min="1"
            max="20"
            value={topBidderLimit}
            onChange={(e) => setTopBidderLimit(Number(e.target.value || 1))}
          />
        </label>
        <button
          className="btn secondary"
          onClick={loadTopBidders}
          disabled={!Number.isInteger(topBidderLimit) || topBidderLimit < 1 || topBidderLimit > 20}
        >
          Load top bidders
        </button>
      </div>

      {topBidders.length ? (
        <div className="card">
          <h4>Top bidders ({topBidderPeriod})</h4>
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>Rank</th>
                  <th>Bidder</th>
                  <th>Highest bid</th>
                  <th>Total value</th>
                  <th>Bid count</th>
                </tr>
              </thead>
              <tbody>
                {topBidders.map((entry) => (
                  <tr key={`${entry.bidderId}-${entry.rank}`}>
                    <td>#{entry.rank}</td>
                    <td>{entry.bidderName || entry.bidderId}</td>
                    <td>{formatCurrency(entry.highestBid)}</td>
                    <td>{formatCurrency(entry.totalBidValue)}</td>
                    <td>{entry.bidCount}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ) : null}

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
