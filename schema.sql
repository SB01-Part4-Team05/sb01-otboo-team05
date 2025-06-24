create type size_type as ENUM(
  'S','M','L','XL','XXL'
);

create type style_type as ENUM(
    'CASUAL','STREET','SPORTY',
    'MINIMAL','CHIC','RETRO';
);

create type color_type as ENUM(
    'WHITE','GRAY','BLACK',
    'BURGUNDY','PINK','CREAM';
);

create type touch_type as ENUM(
    'SOFT','HARD'
);

create type thickness_type as ENUM(
    'THICK','SLIMTHICK','SLIMTHIN','THIN';
);

create type clothes_type as ENUM(
    'TOP','BOTTOM','DRESS','OUTER','UNDERWEAR','ACC',
    'SHOES','SOCKS','CAP','BAG','SCARF';
);

create type user_role as ENUM(
    'USER', 'ADMIN'
);

create type gender_type as ENUM(
    'MALE', 'FEMALE', 'OTHER'
);


CREATE TABLE users
(
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(20) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role user_role DEFAULT 'USER',
    locked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE profiles (
    user_id UUID PRIMARY KEY,
    gender gender_type,
    birth_date DATE,
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    location_x INTEGER,
    location_y INTEGER,
    location_names VARCHAR(255),
    temperature_sensitivity INTEGER,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    profile_image_url VARCHAR(500),
    CONSTRAINT fk_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) on DELETE CASCADE
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) on DELETE CASCADE
);

CREATE TABLE password_resets (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  temporary_password VARCHAR(255) NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  used BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL ,
  constraint fk_password_resets_user FOREIGN KEY (user_id) REFERENCES users(id) on DELETE CASCADE
);

CREATE TABLE weathers
(
    id         UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    user_id UUID NOT NULL,
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
    windSpeedAsWord VARCHAR(20) NOT NULL,
    CONSTRAINT fk_weathers_user Foreign Key (user_id) REFERENCES
        profiles(user_id) ON DELETE CASCADE,
    CONSTRAINT unique_weather UNIQUE (user_id, forecastAt)
);

CREATE TABLE clothes
(
    id UUID primary key,
    attributes UUID not null,
    owner_id UUID not null,
    name varchar(225) not null,
    image_url varchar(225),
    type clothes_type not null,

    foreign key (owner_id) references users(id) on delete cascade,
    foreign key (attributes) references attribute(id)
);

create table attributes
(
    id UUID primary key ,
    size size_type not null ,
    style style_type not null,
    color color_type not null,
    touch touch_type not null,
    thickness thickness_type not null
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
    feed_id UUID NOT NULL REFERENCES feeds(id)
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


CREATE INDEX idx_weathers_user_forecast ON weathers(user_id, forecastAt);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
