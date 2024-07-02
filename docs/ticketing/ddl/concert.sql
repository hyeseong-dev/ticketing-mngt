create table concert
(
    concert_id bigint auto_increment
        primary key,
    created_at datetime(6) not null,
    updated_at datetime(6) null,
    name       varchar(50) not null,
    place_id   bigint      null,
    constraint UKj2huuk6g85bs17nhbuyjx1t91
        unique (place_id)
);

