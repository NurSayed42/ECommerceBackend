-- ============================================================
-- E-Commerce PostgreSQL Database Schema (Auto-created by JPA)
-- This file is for reference. JPA will auto-create tables.
-- ============================================================

-- Create Database (run manually)
-- CREATE DATABASE ecommerce_db;

-- Enable UUID extension (optional)
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Useful indexes (JPA creates basic ones, add these for perf)
-- CREATE INDEX CONCURRENTLY idx_products_search ON products USING gin(to_tsvector('english', name || ' ' || COALESCE(brand, '') || ' ' || COALESCE(tags, '')));
-- CREATE INDEX CONCURRENTLY idx_orders_created ON orders(created_at DESC);
-- CREATE INDEX CONCURRENTLY idx_payments_transaction ON payments(transaction_id);

-- Default Admin Account (created by DataInitializer.java)
-- Email: admin@ecommerce.com
-- Password: admin123
-- Role: SUPER_ADMIN

-- ============================================================
-- Sample Data Queries
-- ============================================================

-- Check all tables
-- SELECT tablename FROM pg_tables WHERE schemaname = 'public';

-- Top selling products
-- SELECT name, sold_count, stock FROM products ORDER BY sold_count DESC LIMIT 10;

-- Revenue by day (last 30 days)
-- SELECT DATE(created_at), COUNT(*), SUM(total_amount)
-- FROM orders WHERE status = 'DELIVERED' AND created_at >= NOW() - INTERVAL '30 days'
-- GROUP BY DATE(created_at) ORDER BY 1;

-- Pending orders count
-- SELECT status, COUNT(*) FROM orders GROUP BY status;
