create type clothes_type as ENUM(
    'TOP','BOTTOM','DRESS','OUTER','UNDERWEAR','ACC',
    'SHOES','SOCKS','CAP','BAG','SCARF'
);

-- 사용자 테이블 (프로필 + 비밀번호 리셋 통합)
CREATE TABLE users
(
    -- 기본 사용자 정보
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(20) NOT NULL,
    password VARCHAR(255) NOT NULL,  -- 임시 비밀번호 요청 시 이 값이 덮어씌워짐
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    locked BOOLEAN DEFAULT FALSE,

    -- 프로필 정보 (초기값 NULL 허용)
    gender VARCHAR(10),
    birth_date DATE,
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    location_x INTEGER,
    location_y INTEGER,
    location_names jsonb,
    temperature_sensitivity INTEGER,
    profile_image_url VARCHAR(500),

    -- 임시 비밀번호 관련
    is_temp_password BOOLEAN DEFAULT FALSE,  -- 현재 비밀번호가 임시 비밀번호인지 여부 추가
    password_expires_at TIMESTAMP,           -- 임시 비밀번호 만료 시간 추가

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- 리프레시 토큰 테이블 (인증 관련) 기존 유지
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);




CREATE TABLE weathers
(
    id         UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    location_x INTEGER,
    location_y INTEGER,
    forecastedAt TIMESTAMP NOT NULL,
    forecastAt TIMESTAMP NOT NULL,
    skyStatus VARCHAR(20) NOT NULL,
    precipitationType VARCHAR(20) NOT NULL,
    precipitationAmount real NOT NULL,
    precipitationProbability real NOT NULL,
    humidityCurrent real NOT NULL,
    humidityComparedToDayBefore real NOT NULL,
    temperatureCurrent real NOT NULL,
    temperatureComparedToDayBefore real NOT NULL,
    temperatureMin real NOT NULL,
    temperatureMax real NOT NULL,
    windSpeed real NOT NULL,
    windSpeedAsWord VARCHAR(20) NOT NULL
);

CREATE TABLE clothes
(
    id UUID primary key,
    owner_id UUID not null,
    name varchar(225) not null,
    image_url varchar(225),
    type clothes_type not null,

    foreign key (owner_id) references users(id) on delete cascade
);

create table attributeValues
(
    id bigint primary key ,
    value varchar(225) not null,
    clothes_id UUID not null,
    definition_id UUID not null,

    foreign key (clothes_id) references clothes(id),
    foreign key (definition_id) references attribute_definition(id)

);

create table attribute_definition
(
  id UUID primary key ,
  name varchar(225) not null
);

CREATE TABLE notifications
(
    id UUID PRIMARY KEY,
    type VARCHAR(30) NOT NULL,
    entity_id UUID,
    title VARCHAR(100),
    content TEXT,
    level VARCHAR(20),
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    receiver_id UUID NOT NULL,
    CONSTRAINT fk_notifications_receiver FOREIGN KEY (receiver_id) REFERENCES users(id)
);

CREATE TABLE follows (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    follower_id UUID NOT NULL,
    followee_id UUID NOT NULL,
    CONSTRAINT fk_follows_follower FOREIGN KEY (follower_id) REFERENCES users(id),
    CONSTRAINT fk_follows_followee FOREIGN KEY (followee_id) REFERENCES users(id),
    CONSTRAINT uq_follows UNIQUE (follower_id, followee_id)
);

CREATE TABLE direct_messages (
    id UUID PRIMARY KEY,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    sender_id UUID NOT NULL,
    receiver_id UUID NOT NULL,
    CONSTRAINT fk_direct_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id),
    CONSTRAINT fk_direct_messages_receiver FOREIGN KEY (receiver_id) REFERENCES users(id)
);

CREATE TABLE feeds
(
    id UUID PRIMARY KEY,
    weather_id UUID NOT NULL REFERENCES weathers(id),
    author_id UUID NOT NULL REFERENCES users(id),
    content TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE ootds
(
    id UUID PRIMARY KEY,
    feed_id UUID NOT NULL REFERENCES feeds(id),
    clothes_id UUID NOT NULL REFERENCES clothes(id)
);

CREATE TABLE feed_comments
(
    id UUID PRIMARY KEY,
    feed_id UUID NOT NULL REFERENCES feeds(id),
    author_id UUID NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE feed_likes
(
    id UUID PRIMARY KEY,
    feed_id UUID NOT NULL REFERENCES feeds(id),
    user_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL,
    UNIQUE (feed_id, user_id)
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);