import { Link } from "react-router-dom";
import { formatCurrency, formatDate } from "../../../shared/utils/formatters";

export default function AuctionCard({ auction }) {
  return (
    <div className="card">
      <div className="module-pill">Auction</div>
      <h3>{auction.auctionId}</h3>
      <div className="inline-meta">
        <span>Item: {auction.itemId}</span>
        <span>Seller: {auction.sellerId}</span>
        <span>Status: {auction.status}</span>
      </div>
      <p style={{ marginTop: "0.8rem" }}>
        Current highest bid: <strong>{formatCurrency(auction.currentHighestBid)}</strong>
      </p>
      <div className="inline-meta">
        <span>Reserve: {formatCurrency(auction.reservePrice)}</span>
        <span>Ends: {formatDate(auction.endTime)}</span>
        <span>Time left: {auction.timeRemainingSeconds ?? 0}s</span>
      </div>
      <div className="card-actions" style={{ marginTop: "1rem" }}>
        <Link className="btn secondary" to={`/auctions/${auction.auctionId}`}>
          View auction
        </Link>
      </div>
    </div>
  );
}
