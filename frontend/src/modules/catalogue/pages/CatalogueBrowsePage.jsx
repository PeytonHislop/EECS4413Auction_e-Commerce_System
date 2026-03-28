import { useEffect, useMemo, useState } from "react";
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

  const filteredItems = useMemo(() => {
    const term = keyword.trim().toLowerCase();
    if (!term) return items;
    return items.filter((item) => {
      const haystack = `${item.name || ""} ${item.description || ""}`.toLowerCase();
      return haystack.includes(term);
    });
  }, [items, keyword]);

  return (
    <div className="page">
      <ModuleHeader
        title="Browse catalogue"
        description="Catalogue owner owns item discovery. Search is currently client-side because the gateway does not forward a keyword parameter yet."
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
