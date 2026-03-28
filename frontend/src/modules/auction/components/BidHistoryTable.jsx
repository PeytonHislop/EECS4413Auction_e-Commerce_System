import { formatCurrency, formatDate } from "../../../shared/utils/formatters";

export default function BidHistoryTable({ bids, title = "Bid history" }) {
  if (!bids?.length) {
    return <div className="empty-state">No bids available.</div>;
  }

  return (
    <div className="card">
      <h3>{title}</h3>
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Bid ID</th>
              <th>Auction ID</th>
              <th>Bidder ID</th>
              <th>Amount</th>
              <th>Timestamp</th>
            </tr>
          </thead>
          <tbody>
            {bids.map((bid) => (
              <tr key={bid.bidId}>
                <td>{bid.bidId}</td>
                <td>{bid.auctionId}</td>
                <td>{bid.bidderId}</td>
                <td>{formatCurrency(bid.bidAmount)}</td>
                <td>{formatDate(bid.bidTimestamp)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
