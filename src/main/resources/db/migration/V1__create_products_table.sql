CREATE TABLE inventory.products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    category VARCHAR(30) NOT NULL,
    stock INTEGER NOT NULL CHECK (stock >= 0),
    price NUMERIC(15, 2) NOT NULL CHECK (price > 0),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);
