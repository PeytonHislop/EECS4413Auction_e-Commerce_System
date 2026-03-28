import React, { useState, useEffect, useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import { getWeeklyLeaderboard, getLeaderboardStats, getBidderStats, handleApiError } from '../services/api';
import './Leaderboard.css';

function Leaderboard() {
  const { user } = useContext(AuthContext);
  const [entries, setEntries] = useState([]);
  const [stats, setStats] = useState(null);
  const [bidderStats, setBidderStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchLeaderboard();
  }, [user]);

  const fetchLeaderboard = async () => {
    setLoading(true);
    setError(null);
    try {
      const [leaderboardRes, statsRes] = await Promise.all([
        getWeeklyLeaderboard(),
        getLeaderboardStats(),
      ]);

      setEntries(leaderboardRes.data.entries || []);
      setStats(statsRes.data);

      // Fetch current user's stats if logged in
      if (user) {
        try {
          const userStatsRes = await getBidderStats(user.username);
          setBidderStats(userStatsRes.data.bidderStats);
        } catch (err) {
          console.log('Could not fetch bidder stats:', err);
        }
      }
    } catch (err) {
      setError(handleApiError(err));
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="leaderboard-page">
      <div className="page-header">
        <h1>Weekly Leaderboard</h1>
        <p>Top bidders for the current week</p>
        <button className="btn btn-primary" onClick={fetchLeaderboard}>
          Refresh
        </button>
      </div>

      {error && <div className="error-banner">{error}</div>}

      {loading ? (
        <div className="loading">Loading leaderboard...</div>
      ) : (
        <>
          {/* Statistics Cards */}
          {stats && (
            <div className="stats-section">
              <div className="stat-card">
                <h3>Highest Bid</h3>
                <p className="stat-value">${stats.highestBid?.toFixed(2) || '0.00'}</p>
              </div>
              <div className="stat-card">
                <h3>Average Bid</h3>
                <p className="stat-value">${stats.averageBid?.toFixed(2) || '0.00'}</p>
              </div>
              <div className="stat-card">
                <h3>Total Bids This Week</h3>
                <p className="stat-value">{stats.totalBids || 0}</p>
              </div>
              <div className="stat-card">
                <h3>Unique Bidders</h3>
                <p className="stat-value">{entries.length}</p>
              </div>
            </div>
          )}

          {/* User's Stats */}
          {user && bidderStats && (
            <div className="user-stats-section">
              <h2>Your Stats This Week</h2>
              <div className="user-stats-grid">
                <div className="user-stat">
                  <span className="label">Your Rank:</span>
                  <span className="value">#{bidderStats.currentWeekRank || '—'}</span>
                </div>
                <div className="user-stat">
                  <span className="label">Your Highest Bid:</span>
                  <span className="value">${bidderStats.highestBid?.toFixed(2) || '0.00'}</span>
                </div>
                <div className="user-stat">
                  <span className="label">Your Average Bid:</span>
                  <span className="value">${bidderStats.averageBid?.toFixed(2) || '0.00'}</span>
                </div>
                <div className="user-stat">
                  <span className="label">Number of Bids:</span>
                  <span className="value">{bidderStats.totalBids || 0}</span>
                </div>
              </div>
            </div>
          )}

          {/* Leaderboard Table */}
          <div className="leaderboard-section">
            <h2>Top 10 Bidders</h2>
            {entries.length === 0 ? (
              <div className="no-data">No bids placed this week yet</div>
            ) : (
              <div className="table-wrapper">
                <table className="leaderboard-table">
                  <thead>
                    <tr>
                      <th className="rank-col">Rank</th>
                      <th className="name-col">Bidder</th>
                      <th className="item-col">Item</th>
                      <th className="amount-col">Bid Amount</th>
                      <th className="time-col">Bid Time</th>
                    </tr>
                  </thead>
                  <tbody>
                    {entries.map((entry, index) => (
                      <tr key={index} className={user?.username === entry.bidderName ? 'highlight' : ''}>
                        <td className="rank">
                          <span className="rank-badge">
                            {entry.rank || index + 1}
                            {entry.rank <= 3 && ['🥇', '🥈', '🥉'][entry.rank - 1]}
                          </span>
                        </td>
                        <td className="name">
                          <strong>{entry.bidderName || 'Anonymous'}</strong>
                          {user?.username === entry.bidderName && (
                            <span className="you-badge">You</span>
                          )}
                        </td>
                        <td className="item">
                          <span className="item-name">
                            {entry.auctionItemName || 'Untitled'}
                          </span>
                        </td>
                        <td className="amount">
                          <span className="amount-value">
                            ${entry.bidAmount?.toFixed(2) || '0.00'}
                          </span>
                        </td>
                        <td className="time">
                          {formatDate(entry.bidTime)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>

          {/* Info Box */}
          <div className="info-box">
            <h4>📊 About the Leaderboard</h4>
            <ul>
              <li>Weekly leaderboard resets every Monday at midnight</li>
              <li>Shows the top 10 highest bids for the current week</li>
              <li>Bids are ranked by amount, with ties broken by time</li>
              <li>Only completed bids are included</li>
              <li>Your personal ranking and stats shown above if logged in</li>
            </ul>
          </div>
        </>
      )}
    </div>
  );
}

export default Leaderboard;
