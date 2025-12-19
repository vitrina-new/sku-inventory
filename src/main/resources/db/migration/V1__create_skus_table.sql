-- SKU Management Service - Initial Schema
-- Creates the main skus table with all required columns and indexes

CREATE TABLE IF NOT EXISTS skus (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sku_code VARCHAR(50) NOT NULL UNIQUE,
    upc VARCHAR(12) UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    brand VARCHAR(100),
    category VARCHAR(50) NOT NULL,
    subcategory VARCHAR(50),
    price NUMERIC(10, 2),
    cost NUMERIC(10, 2),
    unit_of_measure VARCHAR(20),
    quantity_per_unit INTEGER,
    weight NUMERIC(10, 2),
    dimension_length NUMERIC(10, 2),
    dimension_width NUMERIC(10, 2),
    dimension_height NUMERIC(10, 2),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    tags JSONB,
    attributes JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);

-- Create indexes for frequently queried columns
CREATE INDEX idx_sku_code ON skus(sku_code);
CREATE INDEX idx_sku_upc ON skus(upc);
CREATE INDEX idx_sku_category ON skus(category);
CREATE INDEX idx_sku_status ON skus(status);
CREATE INDEX idx_sku_brand ON skus(brand);
CREATE INDEX idx_sku_name ON skus(name);
CREATE INDEX idx_sku_price ON skus(price);
CREATE INDEX idx_sku_created_at ON skus(created_at DESC);

-- Create GIN index for JSONB columns to support containment queries
CREATE INDEX idx_sku_tags ON skus USING GIN (tags);
CREATE INDEX idx_sku_attributes ON skus USING GIN (attributes);

-- Create composite indexes for common query patterns
CREATE INDEX idx_sku_category_status ON skus(category, status);
CREATE INDEX idx_sku_category_price ON skus(category, price);

-- Add check constraint for status values
ALTER TABLE skus ADD CONSTRAINT chk_sku_status
    CHECK (status IN ('ACTIVE', 'DISCONTINUED', 'SEASONAL'));

-- Add check constraint for positive values
ALTER TABLE skus ADD CONSTRAINT chk_sku_price_positive
    CHECK (price IS NULL OR price >= 0);
ALTER TABLE skus ADD CONSTRAINT chk_sku_cost_positive
    CHECK (cost IS NULL OR cost >= 0);
ALTER TABLE skus ADD CONSTRAINT chk_sku_weight_positive
    CHECK (weight IS NULL OR weight >= 0);
ALTER TABLE skus ADD CONSTRAINT chk_sku_quantity_positive
    CHECK (quantity_per_unit IS NULL OR quantity_per_unit >= 1);

COMMENT ON TABLE skus IS 'Stock Keeping Units for retail product management';
COMMENT ON COLUMN skus.sku_code IS 'External SKU code in format THD-{CATEGORY}-{SEQUENCE}';
COMMENT ON COLUMN skus.upc IS 'Universal Product Code (12 digits)';
COMMENT ON COLUMN skus.status IS 'SKU lifecycle status: ACTIVE, DISCONTINUED, SEASONAL';
COMMENT ON COLUMN skus.tags IS 'Searchable product tags stored as JSONB array';
COMMENT ON COLUMN skus.attributes IS 'Flexible key-value attributes stored as JSONB object';
