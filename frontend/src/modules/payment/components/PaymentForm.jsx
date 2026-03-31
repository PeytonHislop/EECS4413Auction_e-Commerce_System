export default function PaymentForm({ form, setForm, onSubmit, loading }) {
  return (
    <form className="card form-grid" onSubmit={onSubmit}>
      <div className="form-grid two">
        <label>
          Auction ID
          <input
            value={form.auctionId}
            onChange={(e) => setForm((prev) => ({ ...prev, auctionId: e.target.value }))}
          />
        </label>

        <label>
          Card number
          <input
            value={form.cardNumber}
            onChange={(e) => setForm((prev) => ({ ...prev, cardNumber: e.target.value }))}
            placeholder="16 digits"
          />
        </label>

        <label>
          Name on card
          <input
            value={form.nameOnCard}
            onChange={(e) => setForm((prev) => ({ ...prev, nameOnCard: e.target.value }))}
          />
        </label>

        <label>
          Expiry (MM/YY)
          <input
            value={form.expiry}
            onChange={(e) => setForm((prev) => ({ ...prev, expiry: e.target.value }))}
            placeholder="08/28"
          />
        </label>

        <label>
          CVV
          <input
            value={form.cvv}
            onChange={(e) => setForm((prev) => ({ ...prev, cvv: e.target.value }))}
            placeholder="123"
          />
        </label>
      </div>

      <button className="btn" disabled={loading}>
        {loading ? "Processing payment..." : "Process payment"}
      </button>
    </form>
  );
}
