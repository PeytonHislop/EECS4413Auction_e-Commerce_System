import { useState } from "react";
import ModuleHeader from "../../../shared/components/ModuleHeader";
import StatusBanner from "../../../shared/components/StatusBanner";
import JsonViewer from "../../../shared/components/JsonViewer";
import { useAuth } from "../../../shared/auth/AuthContext";
import { formatCurrency } from "../../../shared/utils/formatters";
import { paymentApi } from "../api/paymentApi";
import PaymentForm from "../components/PaymentForm";

const initialForm = {
  auctionId: "",
  cardNumber: "",
  nameOnCard: "",
  expiry: "",
  cvv: ""
};

export default function CheckoutPage() {
  const { token, role, userId } = useAuth();
  const [form, setForm] = useState(initialForm);
  const [receipt, setReceipt] = useState(null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError("");
    setSuccess("");
    setReceipt(null);

    try {
      const data = await paymentApi.processPayment(form, token);
      setReceipt(data);
      setSuccess("Payment processed successfully.");
      setForm(initialForm);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page">
      <ModuleHeader
        title="Checkout"
        description="Payment owner owns the payment form and receipt rendering."
        owner="Payment owner"
      />

      <div className="notice">
        Current user: {userId || "Unknown"} · role: {role || "Unknown"}
      </div>

      <StatusBanner error={error} success={success} />
      <PaymentForm form={form} setForm={setForm} onSubmit={handleSubmit} loading={loading} />

      {receipt ? (
        <>
          <div className="card">
            <h3>Receipt summary</h3>
            <div className="inline-meta">
              <span>Order ID: {receipt.orderId}</span>
              <span>Item price: {formatCurrency(receipt.itemPrice)}</span>
              <span>Shipping: {formatCurrency(receipt.shippingCost)}</span>
              <span>Total: {formatCurrency(receipt.total)}</span>
              <span>Shipping days: {receipt.shippingDays}</span>
            </div>
            <p style={{ marginTop: "0.9rem" }}>
              Shipping address: <strong>{receipt.shippingAddress || "—"}</strong>
            </p>
          </div>
          <JsonViewer title="Raw receipt response" data={receipt} />
        </>
      ) : null}
    </div>
  );
}
