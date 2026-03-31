import { useEffect, useState } from "react";
import ModuleHeader from "../../../shared/components/ModuleHeader";
import StatusBanner from "../../../shared/components/StatusBanner";
import JsonViewer from "../../../shared/components/JsonViewer";
import { iamApi } from "../api/iamApi";
import { useAuth } from "../../../shared/auth/AuthContext";

export default function ProfilePage() {
  const { userId, token, username, role } = useAuth();
  const [profile, setProfile] = useState(null);
  const [validation, setValidation] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    let ignore = false;

    async function load() {
      try {
        const [profileData, validationData] = await Promise.all([
          iamApi.getUserProfile(userId, token),
          iamApi.validate(token)
        ]);

        if (!ignore) {
          setProfile(profileData);
          setValidation(validationData);
        }
      } catch (err) {
        if (!ignore) {
          setError(err.message);
        }
      }
    }

    if (userId && token) {
      load();
    }

    return () => {
      ignore = true;
    };
  }, [userId, token]);

  return (
    <div className="page">
      <ModuleHeader
        title="Profile"
        description="IAM owner owns profile rendering and token validation visibility."
        owner="IAM owner"
      />

      <StatusBanner error={error} />

      <div className="grid two">
        <div className="card">
          <h3>Shared auth context</h3>
          <div className="inline-meta">
            <span>Username: {username || "—"}</span>
            <span>Role: {role || "—"}</span>
            <span>User ID: {userId || "—"}</span>
          </div>
        </div>

        <div className="card">
          <h3>Shipping summary</h3>
          {profile?.shippingAddress ? (
            <p>
              {profile.shippingAddress.streetNumber} {profile.shippingAddress.streetName},{" "}
              {profile.shippingAddress.city}, {profile.shippingAddress.country},{" "}
              {profile.shippingAddress.postalCode}
            </p>
          ) : (
            <p>No shipping address available.</p>
          )}
        </div>
      </div>

      {profile && <JsonViewer title="User profile response" data={profile} />}
      {validation && <JsonViewer title="Validate token response" data={validation} />}
    </div>
  );
}
