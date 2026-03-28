import React from 'react';
import './AuctionCard.css';

function AuctionCard({ auction, onBidClick, onViewClick }) {
  const formatTime = (endTime) => {
    const now = new Date();
    const end = new Date(endTime);
    const diff = end - now;

    if (diff < 0) return 'Ended';

    const hours = Math.floor(diff / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));

    if (hours > 24) {
      const days = Math.floor(hours / 24);
      return `${days}d ${hours % 24}h`;
    }
    return `${hours}h ${minutes}m`;
  };

  const timeRemaining = formatTime(auction.endTime);
  const isEnded = new Date(auction.endTime) < new Date();

  return (
    <div className="auction-card">
      <div className="auction-image">
        <div className="placeholder-image">
          <span>📦</span>
        </div>
        {isEnded && <div className="ended-overlay">ENDED</div>}
      </div>

      <div className="auction-content">
        <h3 className="auction-title">{auction.itemName || 'Untitled Item'}</h3>
        
        <p className="auction-description">
          {auction.description ? 
            auction.description.substring(0, 60) + '...' 
            : 'No description'}
        </p>

        <div className="auction-stats">
          <div className="stat">
            <span className="stat-label">Starting Price:</span>
            <span className="stat-value">${auction.startPrice?.toFixed(2)}</span>
          </div>
          <div className="stat">
            <span className="stat-label">Current Bid:</span>
            <span className="stat-value current-bid">
              ${auction.currentHighestBid?.toFixed(2) || '0.00'}
            </span>
          </div>
          <div className="stat">
            <span className="stat-label">Bids:</span>
            <span className="stat-value">{auction.numberOfBids || 0}</span>
          </div>
        </div>

        <div className="auction-footer">
          <div className="time-remaining" style={{
            color: isEnded ? '#999' : (parseInt(timeRemaining) < 24 ? '#e74c3c' : '#27ae60')
          }}>
            ⏱️ {timeRemaining}
          </div>

          <div className="button-group">
            <button 
              className="btn btn-secondary"
              onClick={() => onViewClick(auction.auctionId)}
            >
              View
            </button>
            {!isEnded && (
              <button 
                className="btn btn-primary"
                onClick={() => onBidClick(auction.auctionId)}
              >
                Place Bid ➜
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default AuctionCard;
