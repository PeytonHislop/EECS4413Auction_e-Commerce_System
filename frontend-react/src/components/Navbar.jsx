import React, { useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import './Navbar.css';

function Navbar({ currentPage, setCurrentPage, onLoginClick }) {
  const { user, logout } = useContext(AuthContext);

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <h1 className="brand-title">💎 Code2Cash</h1>
        <span className="brand-subtitle">Auction Platform</span>
      </div>

      <div className="navbar-menu">
        <button 
          className={`nav-link ${currentPage === 'home' ? 'active' : ''}`}
          onClick={() => setCurrentPage('home')}
        >
          Home
        </button>
        
        <button 
          className={`nav-link ${currentPage === 'auctions' ? 'active' : ''}`}
          onClick={() => setCurrentPage('auctions')}
        >
          Browse Auctions
        </button>

        {user && (
          <>
            <button 
              className={`nav-link ${currentPage === 'my-bids' ? 'active' : ''}`}
              onClick={() => setCurrentPage('my-bids')}
            >
              My Bids
            </button>

            {user.role === 'SELLER' && (
              <button 
                className={`nav-link ${currentPage === 'create' ? 'active' : ''}`}
                onClick={() => setCurrentPage('create')}
              >
                Create Auction
              </button>
            )}
          </>
        )}

        <button 
          className={`nav-link ${currentPage === 'leaderboard' ? 'active' : ''}`}
          onClick={() => setCurrentPage('leaderboard')}
        >
          Leaderboard
        </button>
      </div>

      <div className="navbar-auth">
        {user ? (
          <div className="user-info">
            <span className="username">{user.username}</span>
            <span className={`role-badge ${user.role.toLowerCase()}`}>
              {user.role}
            </span>
            <button className="logout-btn" onClick={logout}>
              Logout
            </button>
          </div>
        ) : (
          <button className="login-btn" onClick={onLoginClick}>
            Login
          </button>
        )}
      </div>
    </nav>
  );
}

export default Navbar;
