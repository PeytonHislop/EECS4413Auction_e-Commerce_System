import { useState } from "react";

const AUCTION_TYPES = [
  { value: "FORWARD", label: "Forward Auction" }
  // Add more types if needed
];

export default function ItemForm({ form, setForm, onSubmit, loading }) {
  const [touched, setTouched] = useState({});

  // Simple validation
  const errors = {};
  if (!form.name) errors.name = "Name is required.";
  if (!form.startPrice || Number(form.startPrice) < 1) errors.startPrice = "Start price must be at least 1.";
  if (form.shippingPrice === "" || Number(form.shippingPrice) < 0) errors.shippingPrice = "Shipping price must be 0 or more.";
  if (!form.durationHours || Number(form.durationHours) < 1) errors.durationHours = "Duration must be at least 1 hour.";
  if (!form.description) errors.description = "Description is required.";
  if (!form.auctionType) errors.auctionType = "Auction type is required.";

  const handleBlur = (field) => setTouched((prev) => ({ ...prev, [field]: true }));

  return (
    <form className="card form-grid" onSubmit={onSubmit}>
      <div className="form-grid two">
        <label>
          Item name
          <input
            value={form.name}
            onChange={(e) => setForm((prev) => ({ ...prev, name: e.target.value }))}
            onBlur={() => handleBlur("name")}
          />
          {touched.name && errors.name && <span className="form-error">{errors.name}</span>}
        </label>

        <label>
          Start price
          <input
            type="number"
            min="1"
            step="0.01"
            value={form.startPrice}
            onChange={(e) => setForm((prev) => ({ ...prev, startPrice: e.target.value }))}
            onBlur={() => handleBlur("startPrice")}
          />
          {touched.startPrice && errors.startPrice && <span className="form-error">{errors.startPrice}</span>}
        </label>

        <label>
          Shipping price
          <input
            type="number"
            min="0"
            step="0.01"
            value={form.shippingPrice}
            onChange={(e) => setForm((prev) => ({ ...prev, shippingPrice: e.target.value }))}
            onBlur={() => handleBlur("shippingPrice")}
          />
          {touched.shippingPrice && errors.shippingPrice && <span className="form-error">{errors.shippingPrice}</span>}
        </label>

        <label>
          Duration in hours
          <input
            type="number"
            min="1"
            value={form.durationHours}
            onChange={(e) => setForm((prev) => ({ ...prev, durationHours: e.target.value }))}
            onBlur={() => handleBlur("durationHours")}
          />
          {touched.durationHours && errors.durationHours && <span className="form-error">{errors.durationHours}</span>}
        </label>

        <label>
          Auction type
          <select
            value={form.auctionType || ""}
            onChange={(e) => setForm((prev) => ({ ...prev, auctionType: e.target.value }))}
            onBlur={() => handleBlur("auctionType")}
          >
            <option value="">Select type</option>
            {AUCTION_TYPES.map((type) => (
              <option key={type.value} value={type.value}>{type.label}</option>
            ))}
          </select>
          {touched.auctionType && errors.auctionType && <span className="form-error">{errors.auctionType}</span>}
        </label>
      </div>

      <label>
        Description
        <textarea
          value={form.description}
          onChange={(e) => setForm((prev) => ({ ...prev, description: e.target.value }))}
          onBlur={() => handleBlur("description")}
        />
        {touched.description && errors.description && <span className="form-error">{errors.description}</span>}
      </label>

      <button className="btn" disabled={loading || Object.keys(errors).length > 0}>
        {loading ? "Saving item..." : "Create item"}
      </button>
    </form>
  );
}
