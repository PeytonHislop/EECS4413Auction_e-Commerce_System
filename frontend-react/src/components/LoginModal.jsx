import React, { useState, useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import './LoginModal.css';

function LoginModal({ onClose }) {
  const { login, signup, loading, error } = useContext(AuthContext);
  const [isRegister, setIsRegister] = useState(false);
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    role: 'BUYER',
  });
  const [localError, setLocalError] = useState(null);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value,
    }));
    setLocalError(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLocalError(null);

    if (!formData.username || !formData.password) {
      setLocalError('Username and password are required');
      return;
    }

    try {
      if (isRegister) {
        await signup(formData.username, formData.password, formData.role);
      } else {
        await login(formData.username, formData.password);
      }
      onClose();
    } catch (err) {
      setLocalError(error || 'Authentication failed');
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={e => e.stopPropagation()}>
        <button className="modal-close" onClick={onClose}>×</button>
        
        <div className="modal-header">
          <h2>{isRegister ? 'Create Account' : 'Login'}</h2>
          <p className="modal-subtitle">
            {isRegister 
              ? 'Join our auction platform' 
              : 'Sign in to your account'}
          </p>
        </div>

        <form onSubmit={handleSubmit} className="auth-form">
          {(localError || error) && (
            <div className="error-message">
              {localError || error}
            </div>
          )}

          <div className="form-group">
            <label htmlFor="username">Username</label>
            <input
              id="username"
              type="text"
              name="username"
              value={formData.username}
              onChange={handleChange}
              placeholder="Enter your username"
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              placeholder="Enter your password"
              disabled={loading}
            />
          </div>

          {isRegister && (
            <div className="form-group">
              <label htmlFor="role">Role</label>
              <select
                id="role"
                name="role"
                value={formData.role}
                onChange={handleChange}
                disabled={loading}
              >
                <option value="BUYER">Buyer</option>
                <option value="SELLER">Seller</option>
                <option value="ADMIN">Admin</option>
              </select>
            </div>
          )}

          <button 
            type="submit" 
            className="submit-btn"
            disabled={loading}
          >
            {loading 
              ? 'Processing...' 
              : isRegister ? 'Create Account' : 'Sign In'}
          </button>
        </form>

        <div className="modal-footer">
          <p>
            {isRegister 
              ? 'Already have an account? ' 
              : "Don't have an account? "}
            <button
              type="button"
              className="toggle-btn"
              onClick={() => {
                setIsRegister(!isRegister);
                setLocalError(null);
              }}
              disabled={loading}
            >
              {isRegister ? 'Sign In' : 'Sign Up'}
            </button>
          </p>
        </div>
      </div>
    </div>
  );
}

export default LoginModal;
