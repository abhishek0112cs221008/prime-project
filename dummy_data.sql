-- USERS
INSERT INTO users (name, email, password, role) VALUES 
('Admin User', 'admin@voya.com', 'admin123', 'admin'),
('John Doe', 'john@example.com', 'password', 'customer');

-- PRODUCTS
-- VOYA Premium Bags Collection
INSERT INTO products (name, category, price, quantity, image_url, description, view_count) VALUES 

('The Weekender', 'Travel', 249.00, 50, 
'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&q=80&w=800', 
'The perfect companion for your short getaways. Crafted from durable, water-resistant canvas.', 0),

('Voyager Backpack', 'Everyday', 149.00, 100, 
'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&q=80&w=800', 
'Minimalist design meets maximum utility. Features a dedicated laptop sleeve and anti-theft pockets.', 0),

('Executive Briefcase', 'Business', 399.00, 25, 
'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&q=80&w=800', 
'Premium leather briefcase for the modern professional. Sleek, organized, and timeless.', 0),

('Carry-On Pro', 'Luggage', 295.00, 40, 
'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&q=80&w=800', 
'Polycarbonate hard-shell carry-on with 360-degree spinner wheels and built-in USB charging.', 0),

('Urban Tote', 'Totes', 89.00, 60, 
'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&q=80&w=800', 
'Versatile and spacious. The everyday bag that fits everything you need, from gym gear to groceries.', 0),

('The Duffel', 'Travel', 175.00, 35, 
'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&q=80&w=800', 
'Classic cylindrical design with modern durability. Ideal for the gym or a weekend trip.', 0);
