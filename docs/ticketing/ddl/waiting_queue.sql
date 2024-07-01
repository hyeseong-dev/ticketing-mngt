create table waiting_queue
(
    waiting_queue_id bigint auto_increment
        primary key,
    active_time      datetime(6)                           null,
    request_time     datetime(6)                           null,
    status           enum ('ACTIVE', 'EXPIRED', 'WAITING') not null,
    token            varchar(255)                          not null,
    user_id          bigint                                not null
);

