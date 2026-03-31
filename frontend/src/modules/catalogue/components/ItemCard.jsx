import { Link } from "react-router-dom";
import { formatCurrency, formatDate } from "../../../shared/utils/formatters";

export default function ItemCard({ item }) {
  const getStatusBadge = () => {
    const status = item.status || 'UNKNOWN';
    const styles = {
      ACTIVE: 'status-active',
      PENDING: 'status-pending',
      CLOSED: 'status-closed'
    };
    return <span className={`status-badge ${styles[status] || ''}`}>{status}</span>;
  };

  const getAuctionTypeBadge = () => {
    const type = item.auctionType || 'FORWARD_AUCTION';
    const label = type.replace('_AUCTION', '').replace('_', ' ');
    return <span className="auction-type-badge">{label}</span>;
  };

  return (
    <div className="card item-card">
      <div className="card-header">
        <div className="module-pill">Catalogue</div>
        <div className="badge-group">
          {getStatusBadge()}
          {getAuctionTypeBadge()}
        </div>
      </div>
      <h3 className="item-title">{item.name}</h3>
      <p className="item-description">{item.description}</p>
      <div className="item-pricing">
        <div className="price-box">
          <span className="price-label">Starting Price</span>
          <span className="price-value">{formatCurrency(item.startPrice)}</span>
        </div>
        <div className="price-box">
          <span className="price-label">Shipping</span>
          <span className="price-value">{formatCurrency(item.shippingPrice)}</span>
        </div>
      </div>
      <div className="item-meta">
        <div className="meta-item">
          <svg className="meta-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <span>{item.durationHours}h duration</span>
        </div>
        <div className="meta-item">
          <svg className="meta-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
          <span>Ends {formatDate(item.endDate)}</span>
        </div>
      </div>
      <div className="card-actions">
        <Link className="btn" to={`/catalogue/items/${item.id}`}>
          View Details
        </Link>
      </div>
    </div>
  );
}
