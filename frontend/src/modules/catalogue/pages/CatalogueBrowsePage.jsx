import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import ModuleHeader from "../../../shared/components/ModuleHeader";
import StatusBanner from "../../../shared/components/StatusBanner";
import { catalogueApi } from "../api/catalogueApi";
import ItemCard from "../components/ItemCard";

export default function CatalogueBrowsePage() {
  const [items, setItems] = useState([]);
  const [keyword, setKeyword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let ignore = false;

    async function load() {
      try {
        setLoading(true);
        const data = await catalogueApi.getItems();
        if (!ignore) {
          setItems(Array.isArray(data) ? data : []);
        }
      } catch (err) {
        if (!ignore) {
          setError(err.message);
        }
      } finally {
        if (!ignore) {
          setLoading(false);
        }
      }
    }

    load();
    return () => {
      ignore = true;
    };
  }, []);

  // UC-CAT-2: Filter active items only
  const activeItems = useMemo(() => items.filter(item => !item.status || item.status === 'ACTIVE'), [items]);
  
  // UC-CAT-2.1: Item Search - client-side keyword filtering
  const filteredItems = useMemo(() => {
    const term = keyword.trim().toLowerCase();
    if (!term) return activeItems;
    return activeItems.filter((item) => {
      const haystack = `${item.name || ""} ${item.description || ""}`.toLowerCase();
      return haystack.includes(term);
    });
  }, [activeItems, keyword]);

  return (
    <div className="page">
      <ModuleHeader
        title="Browse Catalogue"
        description="UC-CAT-2: Browse auctioned items · UC-CAT-2.1: Search by keyword"
        owner="Catalogue Service"
      />

      <div className="card search-card">
        <div className="search-header">
          <h3>Find Auction Items</h3>
          <Link className="btn" to="/catalogue/create">
            <svg className="btn-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
            </svg>
            List New Item
          </Link>
        </div>
        <div className="search-box">
          <svg className="search-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          <input
            className="search-input"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="Search by item name or description..."
          />
          {keyword && (
            <button className="search-clear" onClick={() => setKeyword("")}>
              <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          )}
        </div>
        {keyword && (
          <div className="search-info">
            Found {filteredItems.length} item{filteredItems.length !== 1 ? 's' : ''} matching "{keyword}"
          </div>
        )}
      </div>

      <StatusBanner error={error} />

      {loading ? (
        <div className="loading-state">
          <div className="spinner"></div>
          <p>Loading auction items...</p>
        </div>
      ) : (
        <>
          {filteredItems.length > 0 ? (
            <div className="items-grid">
              {filteredItems.map((item) => (
                <ItemCard key={item.id} item={item} />
              ))}
            </div>
          ) : (
            <div className="empty-state-card">
              <svg className="empty-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
              </svg>
              <h3>No Items Found</h3>
              <p>{keyword ? `No items match "${keyword}". Try a different search term.` : "No active auction items available. Be the first to list an item!"}</p>
              <Link className="btn" to="/catalogue/create">List Your First Item</Link>
            </div>
          )}
        </>
      )}
    </div>
  );
}
