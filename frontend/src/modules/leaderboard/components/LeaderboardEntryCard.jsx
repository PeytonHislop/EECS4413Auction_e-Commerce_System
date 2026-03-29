import { formatCurrency, formatDate } from "../../../shared/utils/formatters";

export default function LeaderboardEntryCard({ entry }) {
  return (
    <div className="card">
      <div className="module-pill">Leaderboard</div>
      <h3>#{entry.rank} - {entry.bidderName || entry.bidderId}</h3>
      <div className="inline-meta">
        <span>Auction: {entry.auctionId}</span>
        <span>Item: {entry.itemId}</span>
      </div>
      <p>
        Bid: <strong>{formatCurrency(entry.bidAmount)}</strong>
      </p>
      <div className="inline-meta">
        <span>Seller: {entry.sellerName || entry.sellerId}</span>
        <span>At: {formatDate(entry.bidTime)}</span>
      </div>
    </div>
  );
}
