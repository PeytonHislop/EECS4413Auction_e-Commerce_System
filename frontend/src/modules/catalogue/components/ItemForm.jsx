export default function ItemForm({ form, setForm, onSubmit, loading }) {
  return (
    <form className="card form-grid" onSubmit={onSubmit}>
      <div className="form-grid two">
        <label>
          Item name
          <input
            value={form.name}
            onChange={(e) => setForm((prev) => ({ ...prev, name: e.target.value }))}
          />
        </label>

        <label>
          Start price
          <input
            type="number"
            min="1"
            step="0.01"
            value={form.startPrice}
            onChange={(e) => setForm((prev) => ({ ...prev, startPrice: e.target.value }))}
          />
        </label>

        <label>
          Shipping price
          <input
            type="number"
            min="0"
            step="0.01"
            value={form.shippingPrice}
            onChange={(e) => setForm((prev) => ({ ...prev, shippingPrice: e.target.value }))}
          />
        </label>

        <label>
          Duration in hours
          <input
            type="number"
            min="1"
            value={form.durationHours}
            onChange={(e) => setForm((prev) => ({ ...prev, durationHours: e.target.value }))}
          />
        </label>
      </div>

      <label>
        Description
        <textarea
          value={form.description}
          onChange={(e) => setForm((prev) => ({ ...prev, description: e.target.value }))}
        />
      </label>

      <button className="btn" disabled={loading}>
        {loading ? "Saving item..." : "Create item"}
      </button>
    </form>
  );
}
