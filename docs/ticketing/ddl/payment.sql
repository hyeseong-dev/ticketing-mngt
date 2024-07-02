create table payment
(
    payment_id     bigint auto_increment
        primary key,
    created_at     datetime(6)                                    not null,
    updated_at     datetime(6)                                    null,
    paid_at        datetime(6)                                    null,
    price          decimal(38, 2)                                 not null,
    status         enum ('CANCEL', 'COMPLETE', 'READY', 'REFUND') not null,
    reservation_id bigint                                         null,
    constraint UK3llq7oxcs9j7vlujfdf16jmu
        unique (reservation_id)
);

