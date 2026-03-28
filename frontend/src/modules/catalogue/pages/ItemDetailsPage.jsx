import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import ModuleHeader from "../../../shared/components/ModuleHeader";
import StatusBanner from "../../../shared/components/StatusBanner";
import JsonViewer from "../../../shared/components/JsonViewer";
import { formatCurrency, formatDate } from "../../../shared/utils/formatters";
import { catalogueApi } from "../api/catalogueApi";

export default function ItemDetailsPage() {
  const { itemId } = useParams();
  const [item, setItem] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    let ignore = false;

    async function load() {
      try {
        const data = await catalogueApi.getItemById(itemId);
        if (!ignore) setItem(data);
      } catch (err) {
        if (!ignore) setError(err.message);
      }
    }

    load();
    return () => {
      ignore = true;
    };
  }, [itemId]);

  return (
    <div className="page">
      <ModuleHeader
        title={`Item details · ${itemId}`}
        description="Catalogue owner owns detailed item rendering."
        owner="Catalogue owner"
      />

      <StatusBanner error={error} />

      {item ? (
        <>
          <div className="card">
            <h3>{item.name}</h3>
            <p>{item.description}</p>
            <div className="inline-meta">
              <span>Start price: {formatCurrency(item.startPrice)}</span>
              <span>Shipping: {formatCurrency(item.shippingPrice)}</span>
              <span>Duration: {item.durationHours} hour(s)</span>
              <span>End date: {formatDate(item.endDate)}</span>
              <span>Status: {item.status || "—"}</span>
              <span>Seller ID: {item.sellerId || "—"}</span>
            </div>

            <div className="card-actions" style={{ marginTop: "1rem" }}>
              <Link className="btn secondary" to="/catalogue">
                Back to catalogue
              </Link>
              <Link className="btn" to="/auctions/create">
                Create auction for item
              </Link>
            </div>
          </div>

          <JsonViewer title="Raw item payload" data={item} />
        </>
      ) : !error ? (
        <div className="empty-state">Loading item details...</div>
      ) : null}
    </div>
  );
}
