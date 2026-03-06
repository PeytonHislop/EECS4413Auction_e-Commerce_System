INSERT INTO catalogue_items (name, description, start_price, shipping_price, duration_hours, end_date, status, seller_id, auction_type) 
VALUES ('Gaming Laptop', 'High performance laptop', 1200.00, 25.00, 48, DATEADD('HOUR', 48, CURRENT_TIMESTAMP), 'ACTIVE', 101, 'FORWARD_AUCTION');

INSERT INTO catalogue_items (name, description, start_price, shipping_price, duration_hours, end_date, status, seller_id, auction_type) 
VALUES ('Vintage Watch', 'Gold plated watch', 50.00, 10.00, 24, DATEADD('HOUR', -1, CURRENT_TIMESTAMP), 'EXPIRED', 102, 'FORWARD_AUCTION');

INSERT INTO catalogue_items (name, description, start_price, shipping_price, duration_hours, end_date, status, seller_id, auction_type) 
VALUES ('Smartphone', 'Latest model smartphone', 800.00, 15.00, 72, DATEADD('HOUR', 72, CURRENT_TIMESTAMP), 'ACTIVE', 103, 'FORWARD_AUCTION');
