use ticketing;
INSERT INTO concert (place_id, name, created_at)
VALUES (1, 'Rock Fest 2024', now()),
       (2, 'Jazz Night', now()),
       (3, 'Classical Evening', now()),
       (4, 'Pop Extravaganza', now()),
       (5, 'Hip Hop Live', now());

INSERT INTO concert_date (concert_id, concert_date, created_at)
VALUES (1, '2024-07-01 19:00:00', now()),
       (2, '2024-07-02 19:00:00', now()),
       (3, '2024-07-03 19:00:00', now()),
       (4, '2024-07-04 19:00:00', now()),
       (5, '2024-07-05 19:00:00', now());

-- 좌석 데이터 삽입
INSERT INTO seat (concert_date_id, seat_num, price, status, created_at)
VALUES (1, 1, 50.00, 'AVAILABLE', now()),
       (1, 2, 50.00, 'DISABLE', now()),
       (2, 3, 60.00, 'AVAILABLE', now()),
       (2, 4, 60.00, 'DISABLE', now()),
       (3, 5, 70.00, 'AVAILABLE', now()),
       (3, 6, 70.00, 'DISABLE', now()),
       (4, 7, 80.00, 'AVAILABLE', now()),
       (4, 8, 80.00, 'DISABLE', now()),
       (5, 9, 90.00, 'AVAILABLE', now()),
       (5, 10, 90.00, 'DISABLE', now());