CREATE TABLE orders (
    id            VARCHAR(36)   NOT NULL,
    customer_id   VARCHAR(36)   NOT NULL,
    status        VARCHAR(20)   NOT NULL,
    total_amount  DECIMAL(19,2) NOT NULL,
    placed_at     TIMESTAMP     NOT NULL,
    reject_reason VARCHAR(255),
    CONSTRAINT pk_orders PRIMARY KEY (id)
);

CREATE TABLE order_items (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    order_id     VARCHAR(36)   NOT NULL,
    product_id   VARCHAR(36)   NOT NULL,
    product_name VARCHAR(150)  NOT NULL,
    unit_price   DECIMAL(19,2) NOT NULL,
    quantity     INT           NOT NULL,
    CONSTRAINT pk_order_items PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id)
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);
