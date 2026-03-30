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

  useEffect(() => {
    let ignore = false;

    async function load() {
      try {
        const data = await catalogueApi.getItems();
        if (!ignore) {
          setItems(Array.isArray(data) ? data : []);
        }
      } catch (err) {
        if (!ignore) {
          setError(err.message);
        }
      }
    }

    load();
    return () => {
      ignore = true;
    };
  }, []);

  // Only show items that are currently active (status === 'ACTIVE' or similar)
  const activeItems = useMemo(() => items.filter(item => !item.status || item.status === 'ACTIVE'), [items]);
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
        title="Browse catalogue"
        description="Catalogue owner owns item discovery. Search is currently client-side because the gateway does not forward a keyword parameter yet. Only active items are shown."
        owner="Catalogue owner"
      />

      <div className="card form-grid">
        <label>
          Search items
          <input
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="Search by name or description"
          />
        </label>
        <div style={{ marginTop: "1rem" }}>
          <Link className="btn" to="/catalogue/create">List a new item</Link>
        </div>
      </div>

      <StatusBanner error={error} notice={!error ? `Showing ${filteredItems.length} item(s).` : ""} />

      <div className="grid two">
        {filteredItems.map((item) => (
          <ItemCard key={item.id} item={item} />
        ))}
      </div>

      {!filteredItems.length && !error ? (
        <div className="empty-state">No catalogue items matched your current search.</div>
      ) : null}
    </div>
  );
}
