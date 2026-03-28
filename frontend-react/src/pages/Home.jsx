import React, { useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import './Home.css';

function Home() {
  const { user } = useContext(AuthContext);

  return (
    <div className="home-page">
      <div className="hero-section">
        <div className="hero-content">
          <h1>Welcome to Code2Cash</h1>
          <p className="tagline">The Premier Online Auction Platform</p>
          
          <div className="hero-description">
            <h2>Bid Smart. Win Big. 🏆</h2>
            <p>
              Join thousands of buyers and sellers in the world's most trusted auction marketplace.
              Browse exclusive items, place winning bids, and build your collection.
            </p>
          </div>

          {user ? (
            <div className="user-welcome">
              <p className="welcome-message">
                Welcome back, <strong>{user.username}</strong>!
              </p>
              <p className="role-message">
                You are logged in as a <strong>{user.role}</strong>
              </p>
            </div>
          ) : (
            <p className="login-prompt">
              Login to start bidding or selling today!
            </p>
          )}
        </div>
      </div>

      <div className="stats-section">
        <div className="stat-card">
          <h3>💰</h3>
          <p className="stat-number">$2.5M+</p>
          <p className="stat-label">Total Sales</p>
        </div>
        <div className="stat-card">
          <h3>📦</h3>
          <p className="stat-number">15K+</p>
          <p className="stat-label">Active Items</p>
        </div>
        <div className="stat-card">
          <h3>👥</h3>
          <p className="stat-number">50K+</p>
          <p className="stat-label">Users</p>
        </div>
        <div className="stat-card">
          <h3>⭐</h3>
          <p className="stat-number">4.8/5</p>
          <p className="stat-label">Rating</p>
        </div>
      </div>

      <div className="features-section">
        <h2>Why Choose Code2Cash?</h2>
        <div className="features-grid">
          <div className="feature">
            <h4>🔒 Secure Trading</h4>
            <p>Your transactions are protected with industry-leading security</p>
          </div>
          <div className="feature">
            <h4>⚡ Real-Time Bidding</h4>
            <p>Live auction updates and instant bid notifications</p>
          </div>
          <div className="feature">
            <h4>🌍 Global Marketplace</h4>
            <p>Buy and sell with users worldwide</p>
          </div>
          <div className="feature">
            <h4>📊 Weekly Leaderboard</h4>
            <p>Track top bidders and compete for rankings</p>
          </div>
        </div>
      </div>

      <div className="cta-section">
        <h2>Ready to Get Started?</h2>
        <p>Browse our latest auctions or create your own today</p>
        <div className="cta-buttons">
          <button className="btn btn-primary">Browse Auctions</button>
          <button className="btn btn-secondary">Learn More</button>
        </div>
      </div>
    </div>
  );
}

export default Home;
