import { Navigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export default function ProtectedRoute({ children, roles }) {
  const { isAuthenticated, role } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/iam/login" replace />;
  }

  if (roles?.length && !roles.includes(role)) {
    return (
      <div className="page">
        <div className="page-header">
          <h2>Access denied</h2>
          <p>Your current role does not have access to this page.</p>
        </div>
      </div>
    );
  }

  return children;
}
