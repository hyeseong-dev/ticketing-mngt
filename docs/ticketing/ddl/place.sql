create table place
(
    place_id   bigint auto_increment
        primary key,
    created_at datetime(6)  not null,
    updated_at datetime(6)  null,
    name       varchar(255) not null,
    seats_cnt  int          not null
);

