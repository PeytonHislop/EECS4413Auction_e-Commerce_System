import { Link } from "react-router-dom";
import ModuleHeader from "../../../shared/components/ModuleHeader";
import { moduleRegistry } from "../../../shared/config/moduleRegistry";

const endpointMap = [
  { group: "IAM", routes: ["/api/auth/signup", "/api/auth/login", "/api/auth/validate", "/api/auth/authorize", "/api/auth/password/forgot", "/api/auth/password/reset", "/api/users/{userId}"] },
  { group: "Catalogue", routes: ["/api/items", "/api/items/{id}", "/api/items (POST)"] },
  { group: "Auction", routes: ["/api/auctions/active", "/api/auctions/{auctionId}", "/api/auctions/{auctionId}/bids", "/api/auctions/seller/{sellerId}", "/api/auctions/{auctionId}/close", "/api/auctions/close-expired"] },
  { group: "Payment", routes: ["/api/payments/process"] }
];

export default function GatewayOverviewPage() {
  return (
    <div className="page">
      <ModuleHeader
        title="Gateway overview"
        description="This page makes the React project understandable to the whole team. The browser talks to the gateway, and the gateway fans out to IAM, catalogue, auction, and payment services."
        owner="Gateway owner"
      />

      <div className="grid two">
        <div className="card">
          <h3>How the frontend is split</h3>
          <div className="grid">
            {moduleRegistry.map((module) => (
              <div key={module.key} className="card" style={{ padding: "0.9rem" }}>
                <div className="module-pill">{module.owner}</div>
                <h4 style={{ marginBottom: "0.25rem" }}>{module.key}</h4>
                <p>{module.purpose}</p>
              </div>
            ))}
          </div>
        </div>

        <div className="card">
          <h3>Fast links</h3>
          <div className="card-actions">
            <Link className="btn" to="/iam/login">
              Login
            </Link>
            <Link className="btn secondary" to="/catalogue">
              Browse catalogue
            </Link>
            <Link className="btn secondary" to="/auctions">
              Active auctions
            </Link>
            <Link className="btn secondary" to="/payments/checkout">
              Checkout
            </Link>
          </div>

          <div className="notice" style={{ marginTop: "1rem" }}>
            During development, Vite proxies <code>/api</code> calls to the gateway on port 8080.
          </div>
        </div>
      </div>

      <div className="card">
        <h3>Gateway endpoint map used by React</h3>
        <div className="grid two">
          {endpointMap.map((section) => (
            <div key={section.group} className="card" style={{ padding: "0.9rem" }}>
              <h4>{section.group}</h4>
              <ul>
                {section.routes.map((route) => (
                  <li key={route}>
                    <code>{route}</code>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>
      </div>

      <div className="card">
        <h3>Known backend integration notes</h3>
        <ul>
          <li>Frontend requests go only to the gateway even though each service has its own port.</li>
          <li>Catalogue keyword search is handled client-side because the gateway does not currently pass a keyword parameter through.</li>
          <li>Auction and payment flows depend on role-based JWT behavior, so login is required for seller, buyer, and admin actions.</li>
          <li>The app is intentionally organized by service folder so each teammate can own a clear area.</li>
        </ul>
      </div>
    </div>
  );
}
