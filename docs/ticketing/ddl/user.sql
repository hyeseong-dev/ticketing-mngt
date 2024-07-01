create table user
(
    user_id        bigint auto_increment
        primary key,
    created_at     datetime(6)                 not null,
    updated_at     datetime(6)                 null,
    address        varchar(255)                not null,
    balance        decimal(38, 2) default 0.00 not null,
    deleted_at     datetime(6)                 null,
    email          varchar(255)                not null,
    email_verified bit            default b'0' not null,
    name           varchar(100)                not null,
    password       varchar(255)                not null,
    phone_number   varchar(255)                not null,
    role           enum ('ADMIN', 'USER')      not null,
    constraint UK4bgmpi98dylab6qdvf9xyaxu4
        unique (phone_number),
    constraint UKg4x48cvlxs8po7lbyxrs4s8fk
        unique (address),
    constraint UKob8kqyqqgmefl0aco34akdtpe
        unique (email)
);

