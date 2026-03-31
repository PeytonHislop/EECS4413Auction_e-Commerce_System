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
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let ignore = false;

    async function load() {
      try {
        setLoading(true);
        const data = await catalogueApi.getItemById(itemId);
        if (!ignore) setItem(data);
      } catch (err) {
        if (!ignore) setError(err.message);
      } finally {
        if (!ignore) setLoading(false);
      }
    }

    load();
    return () => {
      ignore = true;
    };
  }, [itemId]);

  const getStatusBadge = (status) => {
    const styles = {
      ACTIVE: 'status-active',
      PENDING: 'status-pending',
      CLOSED: 'status-closed'
    };
    return <span className={`status-badge ${styles[status] || ''}`}>{status}</span>;
  };

  return (
    <div className="page">
      <ModuleHeader
        title={`Item Details`}
        description="Detailed view of auction item information"
        owner="Catalogue Service"
      />

      <StatusBanner error={error} />

      {loading ? (
        <div className="loading-state">
          <div className="spinner"></div>
          <p>Loading item details...</p>
        </div>
      ) : item ? (
        <>
          <div className="card item-details-card">
            <div className="details-header">
              <div>
                <div className="badge-group">
                  {getStatusBadge(item.status || 'UNKNOWN')}
                  <span className="auction-type-badge">
                    {(item.auctionType || 'FORWARD_AUCTION').replace('_AUCTION', '').replace('_', ' ')}
                  </span>
                </div>
                <h2 className="details-title">{item.name}</h2>
                <p className="details-description">{item.description}</p>
              </div>
            </div>

            <div className="details-grid">
              <div className="detail-section">
                <h4>Pricing Information</h4>
                <div className="detail-row">
                  <span className="detail-label">
                    <svg className="detail-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    Starting Price
                  </span>
                  <span className="detail-value price-highlight">{formatCurrency(item.startPrice)}</span>
                </div>
                <div className="detail-row">
                  <span className="detail-label">
                    <svg className="detail-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 8h14M5 8a2 2 0 110-4h14a2 2 0 110 4M5 8v10a2 2 0 002 2h10a2 2 0 002-2V8m-9 4h4" />
                    </svg>
                    Shipping Price
                  </span>
                  <span className="detail-value">{formatCurrency(item.shippingPrice)}</span>
                </div>
              </div>

              <div className="detail-section">
                <h4>Auction Timeline</h4>
                <div className="detail-row">
                  <span className="detail-label">
                    <svg className="detail-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    Duration
                  </span>
                  <span className="detail-value">{item.durationHours} hours</span>
                </div>
                <div className="detail-row">
                  <span className="detail-label">
                    <svg className="detail-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                    End Date
                  </span>
                  <span className="detail-value">{formatDate(item.endDate)}</span>
                </div>
              </div>

              <div className="detail-section">
                <h4>Additional Information</h4>
                <div className="detail-row">
                  <span className="detail-label">Item ID</span>
                  <span className="detail-value">#{item.id}</span>
                </div>
                <div className="detail-row">
                  <span className="detail-label">Seller ID</span>
                  <span className="detail-value">{item.sellerId || "—"}</span>
                </div>
              </div>
            </div>

            <div className="card-actions" style={{ marginTop: "2rem", paddingTop: "1.5rem", borderTop: "1px solid #e5e7eb" }}>
              <Link className="btn secondary" to="/catalogue">
                <svg className="btn-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                </svg>
                Back to Catalogue
              </Link>
              {item.status === 'ACTIVE' && (
                <Link className="btn" to="/auctions/create">
                  <svg className="btn-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z" />
                  </svg>
                  Create Auction
                </Link>
              )}
            </div>
          </div>

          <JsonViewer title="HATEOAS Response (with hypermedia links)" data={item} />
        </>
      ) : null}
    </div>
  );
}
