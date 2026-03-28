import React, { useState, useEffect, useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import { getBidderBids, handleApiError } from '../services/api';
import './MyBids.css';

function MyBids() {
  const { user } = useContext(AuthContext);
  const [bids, setBids] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchMyBids();
  }, [user]);

  const fetchMyBids = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await getBidderBids(user.username);
      setBids(response.data || []);
    } catch (err) {
      setError(handleApiError(err));
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="my-bids-page">
      <div className="page-header">
        <h1>My Bids</h1>
        <p>Track your bidding activity and winning bids</p>
        <button className="btn btn-primary" onClick={fetchMyBids}>
          Refresh
        </button>
      </div>

      {error && <div className="error-banner">{error}</div>}

      {loading ? (
        <div className="loading">Loading your bids...</div>
      ) : bids.length === 0 ? (
        <div className="no-data">
          <p>You haven't placed any bids yet</p>
          <p className="secondary">Browse auctions and place your first bid!</p>
        </div>
      ) : (
        <div className="bids-container">
          <div className="bids-summary">
            <div className="summary-card">
              <h3>Total Bids</h3>
              <p className="big-number">{bids.length}</p>
            </div>
            <div className="summary-card">
              <h3>Highest Bid</h3>
              <p className="big-number">
                ${Math.max(...bids.map(b => b.bidAmount || 0)).toFixed(2)}
              </p>
            </div>
            <div className="summary-card">
              <h3>Average Bid</h3>
              <p className="big-number">
                ${(bids.reduce((sum, b) => sum + (b.bidAmount || 0), 0) / bids.length).toFixed(2)}
              </p>
            </div>
          </div>

          <div className="bids-table-wrapper">
            <table className="bids-table">
              <thead>
                <tr>
                  <th>Auction Item</th>
                  <th>Bid Amount</th>
                  <th>Date Placed</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {bids.map((bid, index) => (
                  <tr key={index}>
                    <td className="item-name">
                      {bid.auctionItemName || 'Untitled Item'}
                    </td>
                    <td className="bid-amount">
                      ${bid.bidAmount?.toFixed(2) || '0.00'}
                    </td>
                    <td className="date">
                      {formatDate(bid.timestamp || bid.bidTime)}
                    </td>
                    <td className="status">
                      <span className={`status-badge ${bid.isWinning ? 'winning' : 'bidding'}`}>
                        {bid.isWinning ? '✓ Winning' : 'Bidding'}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}

export default MyBids;
