CREATE TABLE IF NOT EXISTS socks
(
    id    SERIAL PRIMARY KEY ,
    color  VARCHAR(200) NOT NULL ,
    size VARCHAR(254) NOT NULL ,
    cotton_part BIGINT NOT NULL ,
    quantity BIGINT  NOT NULL
);

CREATE TABLE IF NOT EXISTS transactions
(
    id    SERIAL PRIMARY KEY ,
    type  VARCHAR(200) NOT NULL ,
    time VARCHAR(254) NOT NULL ,
    socks_id BIGINT
);