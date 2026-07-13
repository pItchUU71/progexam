create table if not exists image_submission
(
    id         varchar(255) primary key,
    file_name  varchar(255) not null,
    email      varchar(255) not null,
    created_at timestamp    not null
);
