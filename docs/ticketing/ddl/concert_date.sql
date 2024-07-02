create table concert_date
(
    concert_date_id bigint auto_increment
        primary key,
    created_at      datetime(6) not null,
    updated_at      datetime(6) null,
    concert_date    datetime(6) not null,
    concert_id      bigint      not null
);

