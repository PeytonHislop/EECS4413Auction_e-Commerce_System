import { Link } from "react-router-dom";
import { formatCurrency, formatDate } from "../../../shared/utils/formatters";

export default function ItemCard({ item }) {
  return (
    <div className="card">
      <div className="module-pill">Catalogue</div>
      <h3>{item.name}</h3>
      <p>{item.description}</p>
      <div className="inline-meta">
        <span>Start: {formatCurrency(item.startPrice)}</span>
        <span>Shipping: {formatCurrency(item.shippingPrice)}</span>
        <span>Ends: {formatDate(item.endDate)}</span>
      </div>
      <div className="card-actions" style={{ marginTop: "1rem" }}>
        <Link className="btn secondary" to={`/catalogue/items/${item.id}`}>
          View item
        </Link>
      </div>
    </div>
  );
}
