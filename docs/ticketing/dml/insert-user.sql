USE ticketing;

INSERT INTO user (email, password, name, role, balance, address, phone_number, email_verified, created_at)
VALUES ('test1@gmail.com', '$2a$10$HSWVFQrQtDtV6jfKEigUmOw2SZB8.dZwo7eFcfperLQt45uAUSGKm', 'John Doe', 'USER', 100.00,
        '123 Main St', '010-5897-4855', FALSE, now()),
       ('test2@gmail.com', '$2a$10$HSWVFQrQtDtV6jfKEigUmOw2SZB8.dZwo7eFcfperLQt45uAUSGKm', 'Jane Smith', 'USER', 200.00,
        '456 Oak St', '010-5897-4856', FALSE, now()),
       ('admin1@gmail.com', '$2a$10$HSWVFQrQtDtV6jfKEigUmOw2SZB8.dZwo7eFcfperLQt45uAUSGKm', 'Admin User', 'ADMIN', 0,
        '789 Pine St', '010-5897-4857', TRUE, now()),
       ('admin2@gmail.com', '$2a$10$HSWVFQrQtDtV6jfKEigUmOw2SZB8.dZwo7eFcfperLQt45uAUSGKm', 'Alice Wonderland', 'USER',
        300.00, '321 Maple St', '010-5897-4858', TRUE, now()),
       ('hyeseong43@gmail.com', '$2a$10$HSWVFQrQtDtV6jfKEigUmOw2SZB8.dZwo7eFcfperLQt45uAUSGKm', '이혜성', 'ADMIN', 150.00,
        '654 Birch St', '010-5897-4859', TRUE, now());

