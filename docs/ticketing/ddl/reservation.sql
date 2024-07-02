create table reservation
(
    reservation_id  bigint auto_increment
        primary key,
    created_at      datetime(6)                        not null,
    updated_at      datetime(6)                        null,
    concert_date_id bigint                             not null,
    concert_id      bigint                             not null,
    reserved_at     datetime(6)                        null,
    seat_id         bigint                             not null,
    status          enum ('CANCEL', 'ING', 'RESERVED') not null,
    user_id         bigint                             not null
);

