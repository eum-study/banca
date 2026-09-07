CREATE TABLE products (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_code VARCHAR(30) NOT NULL,
    name VARCHAR(100) NOT NULL,
    sale_status VARCHAR(20) NOT NULL,
    sale_start_at DATETIME NOT NULL,
    sale_end_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_products_product_code (product_code),
    CONSTRAINT chk_products_sale_period
        CHECK (sale_start_at < sale_end_at)
);

CREATE TABLE product_quotas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    total_quantity INT NOT NULL,
    remaining_quantity INT NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_quotas_product_id (product_id),
    CONSTRAINT chk_product_quotas_total_quantity
        CHECK (total_quantity >= 0),
    CONSTRAINT chk_product_quotas_remaining_quantity
        CHECK (remaining_quantity >= 0 AND remaining_quantity <= total_quantity),
    CONSTRAINT fk_product_quotas_product
        FOREIGN KEY (product_id) REFERENCES products (id)
);
