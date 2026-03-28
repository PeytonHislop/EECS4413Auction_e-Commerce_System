import React, { useState, useEffect, useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import AuctionCard from '../components/AuctionCard';
import { getAuctions, placeBid, handleApiError } from '../services/api';
import './AuctionsBrowse.css';

function AuctionsBrowse() {
  const { user } = useContext(AuthContext);
  const [auctions, setAuctions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedAuction, setSelectedAuction] = useState(null);
  const [bidAmount, setBidAmount] = useState('');
  const [bidError, setBidError] = useState(null);
  const [bidSuccess, setBidSuccess] = useState(null);

  useEffect(() => {
    fetchAuctions();
  }, []);

  const fetchAuctions = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await getAuctions(0, 20);
      setAuctions(response.data.content || response.data);
    } catch (err) {
      setError(handleApiError(err));
    } finally {
      setLoading(false);
    }
  };

  const handleBidClick = (auctionId) => {
    if (!user) {
      alert('Please login to place a bid');
      return;
    }
    
    const auction = auctions.find(a => a.auctionId === auctionId);
    setSelectedAuction(auction);
    setBidAmount('');
    setBidError(null);
  };

  const handlePlaceBid = async (e) => {
    e.preventDefault();
    setBidError(null);
    setBidSuccess(null);

    const bidVal = parseFloat(bidAmount);
    const minBid = (selectedAuction.currentHighestBid || selectedAuction.startPrice) + 0.01;

    if (bidVal <= selectedAuction.currentHighestBid || bidVal <= selectedAuction.startPrice) {
      setBidError(`Bid must be higher than $${minBid.toFixed(2)}`);
      return;
    }

    try {
      await placeBid(selectedAuction.auctionId, bidVal);
      setBidSuccess(`Bid of $${bidVal.toFixed(2)} placed successfully!`);
      
      // Refresh auctions
      setTimeout(() => {
        fetchAuctions();
        setSelectedAuction(null);
      }, 1500);
    } catch (err) {
      setBidError(handleApiError(err));
    }
  };

  return (
    <div className="auctions-page">
      <div className="page-header">
        <h1>Browse Auctions</h1>
        <p>Discover amazing items from buyers worldwide</p>
        <button className="btn btn-primary" onClick={fetchAuctions}>
          Refresh
        </button>
      </div>

      {error && <div className="error-banner">{error}</div>}

      {loading ? (
        <div className="loading">Loading auctions...</div>
      ) : auctions.length === 0 ? (
        <div className="no-data">No auctions available</div>
      ) : (
        <div className="auctions-grid">
          {auctions.map(auction => (
            <AuctionCard
              key={auction.auctionId}
              auction={auction}
              onBidClick={handleBidClick}
              onViewClick={(id) => console.log('View auction:', id)}
            />
          ))}
        </div>
      )}

      {selectedAuction && (
        <div className="bid-modal-overlay" onClick={() => setSelectedAuction(null)}>
          <div className="bid-modal" onClick={e => e.stopPropagation()}>
            <button className="close-btn" onClick={() => setSelectedAuction(null)}>×</button>
            
            <h2>Place Bid on {selectedAuction.itemName}</h2>
            
            <div className="bid-info">
              <div className="bid-row">
                <span className="label">Current Highest Bid:</span>
                <span className="value">${selectedAuction.currentHighestBid?.toFixed(2) || '0.00'}</span>
              </div>
              <div className="bid-row">
                <span className="label">Starting Price:</span>
                <span className="value">${selectedAuction.startPrice?.toFixed(2)}</span>
              </div>
              <div className="bid-row">
                <span className="label">Reserve Price:</span>
                <span className="value">${selectedAuction.reservePrice?.toFixed(2)}</span>
              </div>
            </div>

            {bidSuccess && <div className="success-message">{bidSuccess}</div>}
            {bidError && <div className="error-message">{bidError}</div>}

            <form onSubmit={handlePlaceBid}>
              <div className="form-group">
                <label>Your Bid Amount: $</label>
                <input
                  type="number"
                  step="0.01"
                  min={selectedAuction.currentHighestBid ? selectedAuction.currentHighestBid + 0.01 : selectedAuction.startPrice + 0.01}
                  value={bidAmount}
                  onChange={(e) => setBidAmount(e.target.value)}
                  placeholder={`Minimum: $${((selectedAuction.currentHighestBid || selectedAuction.startPrice) + 0.01).toFixed(2)}`}
                  required
                />
              </div>
              <button type="submit" className="btn btn-primary">
                Place Bid
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default AuctionsBrowse;
