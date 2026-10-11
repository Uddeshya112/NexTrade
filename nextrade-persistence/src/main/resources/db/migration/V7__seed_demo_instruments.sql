INSERT INTO instrument (uuid, symbol, name, exchange, currency, lot_size, tick_size, tick_size_tick, status)
VALUES
    ('00000000-0000-4000-8000-000000000101', 'NEX', 'NexTrade Demo', 'NSE', 'INR', 1, 0.05, 0.05, 'ACTIVE'),
    ('00000000-0000-4000-8000-000000000102', 'DEMO', 'Demo Holdings', 'NSE', 'INR', 1, 0.05, 0.05, 'ACTIVE')
ON CONFLICT (symbol) DO NOTHING;
