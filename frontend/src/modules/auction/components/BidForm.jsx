export default function BidForm({ form, setForm, onSubmit, loading }) {
  return (
    <form className="card form-grid" onSubmit={onSubmit}>
      <div className="form-grid two">
        <label>
          Bidder ID
          <input
            value={form.bidderId}
            onChange={(e) => setForm((prev) => ({ ...prev, bidderId: e.target.value }))}
          />
        </label>

        <label>
          Bid amount
          <input
            type="number"
            min="0.01"
            step="0.01"
            value={form.bidAmount}
            onChange={(e) => setForm((prev) => ({ ...prev, bidAmount: e.target.value }))}
          />
        </label>
      </div>

      <button className="btn" disabled={loading}>
        {loading ? "Placing bid..." : "Place bid"}
      </button>
    </form>
  );
}
