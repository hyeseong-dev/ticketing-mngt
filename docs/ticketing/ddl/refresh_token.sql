create table refresh_token
(
    token_id    bigint auto_increment
        primary key,
    device_info varchar(255) null,
    expiry_date datetime(6)  not null,
    ip          varchar(50)  not null,
    token       varchar(255) not null,
    user_id     bigint       not null
);

