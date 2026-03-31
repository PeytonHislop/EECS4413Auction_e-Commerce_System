export default function AuctionForm({ form, setForm, onSubmit, loading }) {
  return (
    <form className="card form-grid" onSubmit={onSubmit}>
      <div className="form-grid two">
        <label>
          Item ID
          <input
            value={form.itemId}
            onChange={(e) => setForm((prev) => ({ ...prev, itemId: e.target.value }))}
            placeholder="Example: 1"
          />
        </label>

        <label>
          Seller ID
          <input
            value={form.sellerId}
            onChange={(e) => setForm((prev) => ({ ...prev, sellerId: e.target.value }))}
            placeholder="Prefilled from auth when available"
          />
        </label>

        <label>
          Duration hours
          <input
            type="number"
            min="1"
            value={form.durationHours}
            onChange={(e) => setForm((prev) => ({ ...prev, durationHours: e.target.value }))}
          />
        </label>

        <label>
          Reserve price
          <input
            type="number"
            min="0"
            step="0.01"
            value={form.reservePrice}
            onChange={(e) => setForm((prev) => ({ ...prev, reservePrice: e.target.value }))}
          />
        </label>
      </div>

      <button className="btn" disabled={loading}>
        {loading ? "Creating auction..." : "Create auction"}
      </button>
    </form>
  );
}
