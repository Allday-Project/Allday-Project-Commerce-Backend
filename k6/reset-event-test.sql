SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE order_users;
TRUNCATE TABLE order_products;
TRUNCATE TABLE orders;
TRUNCATE TABLE product_stock_logs;

SET FOREIGN_KEY_CHECKS = 1;

UPDATE products
SET stock = 100,
    status = 'ON_SALE'
WHERE id = ${PRODUCT_ID};

SELECT id, name, stock, status
FROM products
WHERE id = ${PRODUCT_ID};