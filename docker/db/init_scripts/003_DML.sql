
USE ticketing;

INSERT INTO users (email, password, name, role, balance, address, phone_number, email_verified)
VALUES
    ('test1@gmail.com', '$2a$10$HSWVFQrQtDtV6jfKEigUmOw2SZB8.dZwo7eFcfperLQt45uAUSGKm', 'John Doe', 'USER', 100.00, '123 Main St', '010-5897-4855', FALSE),
    ('test2@gmail.com', '$2a$10$HSWVFQrQtDtV6jfKEigUmOw2SZB8.dZwo7eFcfperLQt45uAUSGKm', 'Jane Smith', 'USER', 200.00, '456 Oak St', '010-5897-4856', FALSE),
    ('admin1@gmail.com', '$2a$10$HSWVFQrQtDtV6jfKEigUmOw2SZB8.dZwo7eFcfperLQt45uAUSGKm', 'Admin User', 'ADMIN', 0, '789 Pine St', '010-5897-4857', TRUE),
    ('admin2@gmail.com', '$2a$10$HSWVFQrQtDtV6jfKEigUmOw2SZB8.dZwo7eFcfperLQt45uAUSGKm', 'Alice Wonderland', 'USER', 300.00, '321 Maple St', '010-5897-4858', TRUE),
    ('hyeseong43@gmail.com', '$2a$10$HSWVFQrQtDtV6jfKEigUmOw2SZB8.dZwo7eFcfperLQt45uAUSGKm', '이혜성', 'ADMIN', 150.00, '654 Birch St', '010-5897-4859', TRUE);


-- INSERT INTO refresh_token (user_id, token, ip, device_info, expiry_date)
-- VALUES
--     (1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoeWVzZW9uZzQzQGdtYWlsLmNvbSIsImlhdCI6MTcxOTE5MTA4MSwiZXhwIjoxNzE5Nzk1ODgxfQ.Ixn1D0iPRPhTHPfX-EezjkAjulMvJcV-mfdXQIQyXOY', '192.168.1.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', '2025-01-01 00:00:00'),
--     (2, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoeWVzZW9uZzQzQGdtYWlsLmNvbSIsImlhdCI6MTcxOTE5MTA4MSwiZXhwIjoxNzE5Nzk1ODgxfQ.Ixn1D0iPRPhTHPfX-EezjkAjulMvJcV-mfdXQIQyXOY', '192.168.1.2', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', '2025-01-01 00:00:00'),
--     (3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoeWVzZW9uZzQzQGdtYWlsLmNvbSIsImlhdCI6MTcxOTE5MTA4MSwiZXhwIjoxNzE5Nzk1ODgxfQ.Ixn1D0iPRPhTHPfX-EezjkAjulMvJcV-mfdXQIQyXOY', '192.168.1.3', 'Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:89.0)', '2025-01-01 00:00:00'),
--     (4, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoeWVzZW9uZzQzQGdtYWlsLmNvbSIsImlhdCI6MTcxOTE5MTA4MSwiZXhwIjoxNzE5Nzk1ODgxfQ.Ixn1D0iPRPhTHPfX-EezjkAjulMvJcV-mfdXQIQyXOY', '192.168.1.4', 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_6 like Mac OS X)', '2025-01-01 00:00:00'),
--     (5, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoeWVzZW9uZzQzQGdtYWlsLmNvbSIsImlhdCI6MTcxOTE5MTA4MSwiZXhwIjoxNzE5Nzk1ODgxfQ.Ixn1D0iPRPhTHPfX-EezjkAjulMvJcV-mfdXQIQyXOY', '192.168.1.5', 'Mozilla/5.0 (Linux; Android 10; SM-G975F)', '2025-01-01 00:00:00'),
--     (1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoeWVzZW9uZzQzQGdtYWlsLmNvbSIsImlhdCI6MTcxOTE5MTA4MSwiZXhwIjoxNzE5Nzk1ODgxfQ.Ixn1D0iPRPhTHPfX-EezjkAjulMvJcV-mfdXQIQyXOY', '192.168.1.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', '2025-01-01 00:00:00'),
--     (2, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoeWVzZW9uZzQzQGdtYWlsLmNvbSIsImlhdCI6MTcxOTE5MTA4MSwiZXhwIjoxNzE5Nzk1ODgxfQ.Ixn1D0iPRPhTHPfX-EezjkAjulMvJcV-mfdXQIQyXOY', '192.168.1.2', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', '2025-01-01 00:00:00'),
--     (3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoeWVzZW9uZzQzQGdtYWlsLmNvbSIsImlhdCI6MTcxOTE5MTA4MSwiZXhwIjoxNzE5Nzk1ODgxfQ.Ixn1D0iPRPhTHPfX-EezjkAjulMvJcV-mfdXQIQyXOY', '192.168.1.3', 'Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:89.0)', '2025-01-01 00:00:00'),
--     (4, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoeWVzZW9uZzQzQGdtYWlsLmNvbSIsImlhdCI6MTcxOTE5MTA4MSwiZXhwIjoxNzE5Nzk1ODgxfQ.Ixn1D0iPRPhTHPfX-EezjkAjulMvJcV-mfdXQIQyXOY', '192.168.1.4', 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_6 like Mac OS X)', '2025-01-01 00:00:00'),
--     (5, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoeWVzZW9uZzQzQGdtYWlsLmNvbSIsImlhdCI6MTcxOTE5MTA4MSwiZXhwIjoxNzE5Nzk1ODgxfQ.Ixn1D0iPRPhTHPfX-EezjkAjulMvJcV-mfdXQIQyXOY', '192.168.1.5', 'Mozilla/5.0 (Linux; Android 10; SM-G975F)', '2025-01-01 00:00:00');

INSERT INTO place (name, seats_cnt)
VALUES
    ('Grand Hall', 500),
    ('Open Air Theatre', 300),
    ('City Auditorium', 400),
    ('Downtown Arena', 600),
    ('Metropolitan Opera House', 700);

INSERT INTO concert (place_id, name)
VALUES
    (1, 'Rock Fest 2024'),
    (2, 'Jazz Night'),
    (3, 'Classical Evening'),
    (4, 'Pop Extravaganza'),
    (5, 'Hip Hop Live');

INSERT INTO concert_date (concert_id, place_id, concert_date)
VALUES
    (1, 1, '2024-07-01 19:00:00'),
    (2, 2, '2024-07-02 19:00:00'),
    (3, 3, '2024-07-03 19:00:00'),
    (4, 4, '2024-07-04 19:00:00'),
    (5, 5, '2024-07-05 19:00:00');

INSERT INTO seat (concert_date_id, place_id, seat_num, price, status)
VALUES
    (1, 1, 1, 50.00, 'SEAT_AVAILABLE'),
    (1, 1, 2, 50.00, 'SEAT_RESERVED'),
    (2, 2, 3, 60.00, 'SEAT_AVAILABLE'),
    (2, 2, 4, 60.00, 'SEAT_RESERVED'),
    (3, 3, 5, 70.00, 'SEAT_AVAILABLE'),
    (3, 3, 6, 70.00, 'SEAT_RESERVED'),
    (4, 4, 7, 80.00, 'SEAT_AVAILABLE'),
    (4, 4, 8, 80.00, 'SEAT_RESERVED'),
    (5, 5, 9, 90.00, 'SEAT_AVAILABLE'),
    (5, 5, 10, 90.00, 'SEAT_RESERVED');

INSERT INTO reservation (status, user_id, concert_date_id, seat_id, reserved_at)
VALUES
    ('PENDING', 1, 1, 1, '2024-06-01 10:00:00'),
    ('CONFIRMED', 2, 2, 3, '2024-06-02 11:00:00'),
    ('CANCELLED', 3, 3, 5, '2024-06-03 12:00:00'),
    ('PENDING', 4, 4, 7, '2024-06-04 13:00:00'),
    ('CONFIRMED', 5, 5, 9, '2024-06-05 14:00:00'),
    ('PENDING', 1, 1, 2, '2024-06-01 10:15:00'),
    ('CONFIRMED', 2, 2, 4, '2024-06-02 11:15:00'),
    ('CANCELLED', 3, 3, 6, '2024-06-03 12:15:00'),
    ('PENDING', 4, 4, 8, '2024-06-04 13:15:00'),
    ('CONFIRMED', 5, 5, 10, '2024-06-05 14:15:00');

INSERT INTO payment (reservation_id, payment_price, status, paid_at)
VALUES
    (1, 50.00, 'PAYMENT_READY', '2024-06-01 10:05:00'),
    (2, 60.00, 'PAID', '2024-06-02 11:05:00'),
    (3, 70.00, 'CANCELLED', '2024-06-03 12:05:00'),
    (4, 80.00, 'PAYMENT_READY', '2024-06-04 13:05:00'),
    (5, 90.00, 'PAID', '2024-06-05 14:05:00'),
    (6, 50.00, 'PAYMENT_READY', '2024-06-01 10:20:00'),
    (7, 60.00, 'PAID', '2024-06-02 11:20:00'),
    (8, 70.00, 'CANCELLED', '2024-06-03 12:20:00'),
    (9, 80.00, 'PAYMENT_READY', '2024-06-04 13:20:00'),
    (10, 90.00, 'PAID', '2024-06-05 14:20:00');

INSERT INTO waiting_queue (user_id, token, status)
VALUES
    (1, 'wait_token1', 'WAITING'),
    (2, 'wait_token2', 'WAITING'),
    (3, 'wait_token3', 'WAITING'),
    (4, 'wait_token4', 'WAITING'),
    (5, 'wait_token5', 'WAITING'),
    (1, 'enter_token1', 'ENTERED'),
    (2, 'enter_token2', 'ENTERED'),
    (3, 'enter_token3', 'ENTERED'),
    (4, 'enter_token4', 'ENTERED'),
    (5, 'enter_token5', 'ENTERED');
