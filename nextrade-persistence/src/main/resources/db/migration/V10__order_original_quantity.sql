ALTER TABLE orders ADD COLUMN original_quantity NUMERIC(19, 4);
UPDATE orders SET original_quantity = quantity WHERE original_quantity IS NULL;
ALTER TABLE orders ALTER COLUMN original_quantity SET NOT NULL;
