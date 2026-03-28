import React, { useState } from 'react';
import { createAuction, handleApiError } from '../services/api';
import './CreateAuction.css';

function CreateAuction() {
  const [formData, setFormData] = useState({
    itemName: '',
    description: '',
    startPrice: '',
    reservePrice: '',
    shippingPrice: '',
    durationHours: '72',
  });
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name.includes('Price') || name.includes('Duration') 
        ? parseFloat(value) || ''
        : value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    // Validation
    if (!formData.itemName || !formData.description || !formData.startPrice) {
      setError('Please fill in all required fields');
      return;
    }

    if (formData.startPrice <= 0 || formData.reservePrice <= 0) {
      setError('Prices must be greater than 0');
      return;
    }

    if (formData.reservePrice < formData.startPrice) {
      setError('Reserve price must be greater than or equal to starting price');
      return;
    }

    setLoading(true);
    try {
      const payload = {
        name: formData.itemName,
        description: formData.description,
        startPrice: formData.startPrice,
        reservePrice: formData.reservePrice,
        shippingPrice: formData.shippingPrice || 0,
        durationHours: parseInt(formData.durationHours),
      };

      await createAuction(payload);
      
      setSuccess('Auction created successfully! 🎉');
      setFormData({
        itemName: '',
        description: '',
        startPrice: '',
        reservePrice: '',
        shippingPrice: '',
        durationHours: '72',
      });

      // Clear success message after 3 seconds
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError(handleApiError(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="create-auction-page">
      <div className="page-header">
        <h1>Create New Auction</h1>
        <p>List your item for auction</p>
      </div>

      <div className="form-container">
        {error && <div className="error-banner">{error}</div>}
        {success && <div className="success-banner">{success}</div>}

        <form onSubmit={handleSubmit} className="create-form">
          <div className="form-section">
            <h3>Item Details</h3>

            <div className="form-group">
              <label htmlFor="itemName">Item Name *</label>
              <input
                id="itemName"
                type="text"
                name="itemName"
                value={formData.itemName}
                onChange={handleChange}
                placeholder="e.g., Vintage Gold Watch"
                required
                disabled={loading}
              />
            </div>

            <div className="form-group">
              <label htmlFor="description">Description *</label>
              <textarea
                id="description"
                name="description"
                value={formData.description}
                onChange={handleChange}
                placeholder="Detailed description of the item, condition, etc."
                rows="6"
                required
                disabled={loading}
              />
            </div>
          </div>

          <div className="form-section">
            <h3>Pricing</h3>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="startPrice">Starting Price ($) *</label>
                <input
                  id="startPrice"
                  type="number"
                  name="startPrice"
                  step="0.01"
                  min="0"
                  value={formData.startPrice}
                  onChange={handleChange}
                  placeholder="100.00"
                  required
                  disabled={loading}
                />
              </div>

              <div className="form-group">
                <label htmlFor="reservePrice">Reserve Price ($) *</label>
                <input
                  id="reservePrice"
                  type="number"
                  name="reservePrice"
                  step="0.01"
                  min="0"
                  value={formData.reservePrice}
                  onChange={handleChange}
                  placeholder="500.00"
                  required
                  disabled={loading}
                />
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="shippingPrice">Shipping Cost ($)</label>
              <input
                id="shippingPrice"
                type="number"
                name="shippingPrice"
                step="0.01"
                min="0"
                value={formData.shippingPrice}
                onChange={handleChange}
                placeholder="0.00"
                disabled={loading}
              />
            </div>
          </div>

          <div className="form-section">
            <h3>Auction Duration</h3>

            <div className="form-group">
              <label htmlFor="durationHours">Duration (Hours) *</label>
              <select
                id="durationHours"
                name="durationHours"
                value={formData.durationHours}
                onChange={handleChange}
                disabled={loading}
              >
                <option value="24">24 hours</option>
                <option value="48">48 hours (2 days)</option>
                <option value="72">72 hours (3 days)</option>
                <option value="168">168 hours (1 week)</option>
              </select>
            </div>
          </div>

          <div className="form-actions">
            <button 
              type="submit" 
              className="btn btn-primary btn-large"
              disabled={loading}
            >
              {loading ? 'Creating Auction...' : 'Create Auction'}
            </button>
            <button 
              type="reset"
              className="btn btn-secondary"
              disabled={loading}
            >
              Clear Form
            </button>
          </div>
        </form>

        <div className="info-box">
          <h4>📋 Tips for Success</h4>
          <ul>
            <li>Use clear, descriptive titles that highlight the item</li>
            <li>Provide detailed descriptions including condition and measurements</li>
            <li>Set a competitive starting price to attract bidders</li>
            <li>Reserve price protects your item if bids are too low</li>
            <li>Choose longer auction durations to reach more bidders</li>
          </ul>
        </div>
      </div>
    </div>
  );
}

export default CreateAuction;
