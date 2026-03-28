import { Navigate, Route, Routes } from "react-router-dom";
import Layout from "./shared/components/Layout";
import { AuthProvider } from "./shared/auth/AuthContext";
import ProtectedRoute from "./shared/components/ProtectedRoute";

import GatewayOverviewPage from "./modules/gateway/pages/GatewayOverviewPage";

import LoginPage from "./modules/iam/pages/LoginPage";
import SignupPage from "./modules/iam/pages/SignupPage";
import ProfilePage from "./modules/iam/pages/ProfilePage";
import AuthToolsPage from "./modules/iam/pages/AuthToolsPage";

import CatalogueBrowsePage from "./modules/catalogue/pages/CatalogueBrowsePage";
import ItemDetailsPage from "./modules/catalogue/pages/ItemDetailsPage";
import CreateItemPage from "./modules/catalogue/pages/CreateItemPage";

import ActiveAuctionsPage from "./modules/auction/pages/ActiveAuctionsPage";
import AuctionDetailsPage from "./modules/auction/pages/AuctionDetailsPage";
import CreateAuctionPage from "./modules/auction/pages/CreateAuctionPage";
import SellerAuctionsPage from "./modules/auction/pages/SellerAuctionsPage";
import BidderBidsPage from "./modules/auction/pages/BidderBidsPage";
import AdminAuctionsPage from "./modules/auction/pages/AdminAuctionsPage";

import CheckoutPage from "./modules/payment/pages/CheckoutPage";

export default function App() {
  return (
    <AuthProvider>
      <Layout>
        <Routes>
          <Route path="/" element={<GatewayOverviewPage />} />

          <Route path="/iam/login" element={<LoginPage />} />
          <Route path="/iam/signup" element={<SignupPage />} />
          <Route
            path="/iam/profile"
            element={
              <ProtectedRoute>
                <ProfilePage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/iam/tools"
            element={
              <ProtectedRoute>
                <AuthToolsPage />
              </ProtectedRoute>
            }
          />

          <Route path="/catalogue" element={<CatalogueBrowsePage />} />
          <Route path="/catalogue/items/:itemId" element={<ItemDetailsPage />} />
          <Route
            path="/catalogue/create"
            element={
              <ProtectedRoute roles={["SELLER", "ADMIN"]}>
                <CreateItemPage />
              </ProtectedRoute>
            }
          />

          <Route path="/auctions" element={<ActiveAuctionsPage />} />
          <Route path="/auctions/:auctionId" element={<AuctionDetailsPage />} />
          <Route
            path="/auctions/create"
            element={
              <ProtectedRoute roles={["SELLER", "ADMIN"]}>
                <CreateAuctionPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/auctions/seller"
            element={
              <ProtectedRoute roles={["SELLER", "ADMIN"]}>
                <SellerAuctionsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/auctions/my-bids"
            element={
              <ProtectedRoute roles={["BUYER", "ADMIN"]}>
                <BidderBidsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/auctions/admin"
            element={
              <ProtectedRoute roles={["ADMIN"]}>
                <AdminAuctionsPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/payments/checkout"
            element={
              <ProtectedRoute roles={["BUYER", "ADMIN"]}>
                <CheckoutPage />
              </ProtectedRoute>
            }
          />

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Layout>
    </AuthProvider>
  );
}
