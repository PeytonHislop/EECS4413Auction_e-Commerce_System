import React, { useState, useContext } from 'react';
import './App.css';
import { AuthContext } from './context/AuthContext';
import Navbar from './components/Navbar';
import LoginModal from './components/LoginModal';
import Home from './pages/Home';
import AuctionsBrowse from './pages/AuctionsBrowse';
import MyBids from './pages/MyBids';
import CreateAuction from './pages/CreateAuction';
import Leaderboard from './pages/Leaderboard';

function App() {
  const { user } = useContext(AuthContext);
  const [currentPage, setCurrentPage] = useState('home');
  const [showLoginModal, setShowLoginModal] = useState(false);

  const renderPage = () => {
    switch (currentPage) {
      case 'home':
        return <Home />;
      case 'auctions':
        return <AuctionsBrowse />;
      case 'my-bids':
        return user ? <MyBids /> : <Home />;
      case 'create':
        return user?.role === 'SELLER' ? <CreateAuction /> : <Home />;
      case 'leaderboard':
        return <Leaderboard />;
      default:
        return <Home />;
    }
  };

  return (
    <div className="App">
      <Navbar 
        currentPage={currentPage}
        setCurrentPage={setCurrentPage}
        onLoginClick={() => setShowLoginModal(true)}
      />
      
      {showLoginModal && (
        <LoginModal onClose={() => setShowLoginModal(false)} />
      )}

      <main className="main-content">
        {renderPage()}
      </main>

      <footer className="footer">
        <p>&copy; 2026 Code2Cash Auction Platform. All rights reserved.</p>
      </footer>
    </div>
  );
}

export default App;
