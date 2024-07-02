create table seat
(
    seat_id         bigint auto_increment
        primary key,
    created_at      datetime(6)                   not null,
    updated_at      datetime(6)                   null,
    price           decimal(38, 2)                not null,
    seat_num        int                           not null,
    status          enum ('AVAILABLE', 'DISABLE') not null,
    concert_date_id bigint                        not null
);

