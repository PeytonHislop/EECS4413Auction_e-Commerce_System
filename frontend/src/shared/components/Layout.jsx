import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

const sections = [
  {
    title: "Gateway",
    links: [{ to: "/", label: "Overview" }]
  },
  {
    title: "IAM",
    links: [
      { to: "/iam/login", label: "Login" },
      { to: "/iam/signup", label: "Signup" },
      { to: "/iam/profile", label: "Profile" },
      { to: "/iam/tools", label: "Auth Tools" }
    ]
  },
  {
    title: "Catalogue",
    links: [
      { to: "/catalogue", label: "Browse Items" },
      { to: "/catalogue/create", label: "Create Item" }
    ]
  },
  {
    title: "Auction",
    links: [
      { to: "/auctions", label: "Active Auctions" },
      { to: "/auctions/create", label: "Create Auction" },
      { to: "/auctions/seller", label: "Seller Auctions" },
      { to: "/auctions/my-bids", label: "My Bids" },
      { to: "/auctions/admin", label: "Admin Tools" }
    ]
  },
  {
    title: "Payment",
    links: [{ to: "/payments/checkout", label: "Checkout" }]
  },
  {
    title: "Leaderboard",
    links: [{ to: "/leaderboard", label: "Leaderboard" }]
  }
];

export default function Layout({ children }) {
  const { isAuthenticated, username, role, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <h1>Code2Cash Frontend</h1>
        <p>Organized by service ownership so each teammate knows exactly where to work.</p>

        {sections.map((section) => (
          <div className="sidebar-section" key={section.title}>
            <h2>{section.title}</h2>
            <div className="nav-list">
              {section.links.map((link) => (
                <NavLink
                  key={link.to}
                  to={link.to}
                  className={({ isActive }) =>
                    `nav-link${isActive ? " active" : ""}`
                  }
                >
                  {link.label}
                </NavLink>
              ))}
            </div>
          </div>
        ))}
      </aside>

      <main className="shell-main">
        <div className="topbar">
          <div>
            <div className="badge">Gateway-first React app</div>
          </div>

          <div className="topbar-actions">
            {isAuthenticated ? (
              <>
                <span className="badge">
                  {username} · {role}
                </span>
                <button
                  className="btn secondary"
                  onClick={() => navigate("/iam/profile")}
                >
                  My Profile
                </button>
                <button
                  className="btn secondary"
                  onClick={() => {
                    logout();
                    navigate("/iam/login");
                  }}
                >
                  Logout
                </button>
              </>
            ) : (
              <>
                <button className="btn secondary" onClick={() => navigate("/iam/login")}>
                  Login
                </button>
                <button className="btn" onClick={() => navigate("/iam/signup")}>
                  Signup
                </button>
              </>
            )}
          </div>
        </div>

        {children}
      </main>
    </div>
  );
}
