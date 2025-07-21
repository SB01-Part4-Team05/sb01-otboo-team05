create type clothes_type as ENUM (
    'TOP','BOTTOM','DRESS','OUTER','UNDERWEAR','ACC',
    'SHOES','SOCKS','CAP','BAG','SCARF'
    );

create type notification_type as ENUM (
    'ROLE_CHANGED',
    'SUSPICIOUS_ACTIVITY',
    'LIKE_OR_COMMENT_ON_FEED',
    'FOLLOWEE_CREATED_FEED',
    'NEW_FOLLOWER',
    'RECEIVED_DM',
    'RECEIVED_COMMENT'
    );

create type notification_level as ENUM (
    'INFO',
    'WARNING',
    'ERROR'
    );

-- 사용자 테이블 (프로필 + 비밀번호 리셋 통합)
CREATE TABLE users
(
    -- 기본 사용자 정보
    id                      UUID PRIMARY KEY,
    email                   VARCHAR(255) UNIQUE NOT NULL,
    name                    VARCHAR(20)         NOT NULL,
    password                VARCHAR(255)        NOT NULL,               -- 임시 비밀번호 요청 시 이 값이 덮어씌워짐
    role                    VARCHAR(20)         NOT NULL DEFAULT 'USER',
    locked                  BOOLEAN                      DEFAULT FALSE,

    -- 프로필 정보 (초기값 NULL 허용)
    gender                  VARCHAR(10),
    birth_date              DATE,
    latitude                DOUBLE PRECISION,
    longitude               DOUBLE PRECISION,
    location_x              INTEGER,
    location_y              INTEGER,
    location_names          jsonb,
    temperature_sensitivity INTEGER,
    profile_image_url       VARCHAR(500),

    -- 임시 비밀번호 관련
    is_temp_password        BOOLEAN                      DEFAULT FALSE, -- 현재 비밀번호가 임시 비밀번호인지 여부 추가
    password_expires_at     TIMESTAMP,                                  -- 임시 비밀번호 만료 시간 추가

    created_at              TIMESTAMP           NOT NULL,
    updated_at              TIMESTAMP
);

-- 리프레시 토큰 테이블 (인증 관련) 기존 유지
CREATE TABLE refresh_tokens
(
    id         UUID PRIMARY KEY,
    user_id    UUID                NOT NULL,
    token      VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP           NOT NULL,
    revoked    BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP           NOT NULL,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);



CREATE TABLE weathers
(
    id                                 UUID PRIMARY KEY,
    location_x                         INTEGER          NOT NULL,
    location_y                         INTEGER          NOT NULL,
    forecasted_at                      TIMESTAMP        NOT NULL,
    forecast_at                        TIMESTAMP        NOT NULL,
    sky_status                         VARCHAR(20)      NOT NULL,
    precipitation_type                 VARCHAR(20)      NOT NULL,
    precipitation_amount               DOUBLE PRECISION NOT NULL,
    precipitation_probability          DOUBLE PRECISION NOT NULL,
    humidity_current                   DOUBLE PRECISION NOT NULL,
    humidity_compared_to_day_before    DOUBLE PRECISION,
    temperature_current                DOUBLE PRECISION NOT NULL,
    temperature_compared_to_day_before DOUBLE PRECISION,
    temperature_min                    DOUBLE PRECISION NOT NULL,
    temperature_max                    DOUBLE PRECISION NOT NULL,
    wind_speed                         DOUBLE PRECISION NOT NULL,
    wind_speed_as_word                 VARCHAR(20)      NOT NULL
);

CREATE TABLE clothes
(
    id        UUID primary key,
    owner_id  UUID         not null,
    name      varchar(225) not null,
    image_url varchar(225),
    type      clothes_type not null,

    foreign key (owner_id) references users (id) on delete cascade
);

create table attribute_definition
(
    id   UUID primary key,
    name varchar(225) not null
);

create table attribute_values
(
    id            bigint primary key,
    value         varchar(225) not null,
    clothes_id    UUID         not null,
    definition_id UUID         not null,

    foreign key (clothes_id) references clothes (id),
    foreign key (definition_id) references attribute_definition (id)

);

CREATE TABLE notifications
(
    id          UUID PRIMARY KEY,
    type        notification_type NOT NULL,
    entity_id   UUID,
    title       VARCHAR(100),
    content     TEXT,
    level       notification_level,
    is_read     BOOLEAN DEFAULT FALSE,
    created_at  TIMESTAMP         NOT NULL,
    receiver_id UUID              NOT NULL,
    CONSTRAINT fk_notifications_receiver FOREIGN KEY (receiver_id) REFERENCES users (id)
);

CREATE TABLE follows
(
    id          UUID PRIMARY KEY,
    created_at  TIMESTAMP NOT NULL,
    follower_id UUID      NOT NULL,
    followee_id UUID      NOT NULL,
    CONSTRAINT fk_follows_follower FOREIGN KEY (follower_id) REFERENCES users (id),
    CONSTRAINT fk_follows_followee FOREIGN KEY (followee_id) REFERENCES users (id),
    CONSTRAINT uq_follows UNIQUE (follower_id, followee_id)
);

CREATE TABLE direct_messages
(
    id          UUID PRIMARY KEY,
    content     TEXT      NOT NULL,
    created_at  TIMESTAMP NOT NULL,
    sender_id   UUID      NOT NULL,
    receiver_id UUID      NOT NULL,
    CONSTRAINT fk_direct_messages_sender FOREIGN KEY (sender_id) REFERENCES users (id),
    CONSTRAINT fk_direct_messages_receiver FOREIGN KEY (receiver_id) REFERENCES users (id)
);

CREATE TABLE feeds
(
    id         UUID PRIMARY KEY,
    weather_id UUID      NOT NULL REFERENCES weathers (id),
    author_id  UUID      NOT NULL REFERENCES users (id),
    content    TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    like_count BIGINT
);

CREATE TABLE ootds
(
    id         UUID PRIMARY KEY,
    feed_id    UUID NOT NULL REFERENCES feeds (id),
    clothes_id UUID NOT NULL REFERENCES clothes (id)
);

CREATE TABLE feed_comments
(
    id         UUID PRIMARY KEY,
    feed_id    UUID      NOT NULL REFERENCES feeds (id),
    author_id  UUID      NOT NULL REFERENCES users (id),
    content    TEXT      NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE feed_likes
(
    id         UUID PRIMARY KEY,
    feed_id    UUID      NOT NULL REFERENCES feeds (id),
    user_id    UUID      NOT NULL REFERENCES users (id),
    created_at TIMESTAMP NOT NULL,
    UNIQUE (feed_id, user_id)
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens (token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_weather_location_forecast_latest ON weathers (location_x, location_y, forecast_at, forecasted_at DESC);
CREATE INDEX idx_weather_forecasted_at ON weathers (forecasted_at);

-- 스프링 배치 테이블
CREATE TABLE BATCH_JOB_INSTANCE
(
    JOB_INSTANCE_ID BIGINT       NOT NULL PRIMARY KEY,
    VERSION         BIGINT,
    JOB_NAME        VARCHAR(100) NOT NULL,
    JOB_KEY         VARCHAR(32)  NOT NULL,
    constraint JOB_INST_UN unique (JOB_NAME, JOB_KEY)
);

CREATE TABLE BATCH_JOB_EXECUTION
(
    JOB_EXECUTION_ID BIGINT    NOT NULL PRIMARY KEY,
    VERSION          BIGINT,
    JOB_INSTANCE_ID  BIGINT    NOT NULL,
    CREATE_TIME      TIMESTAMP NOT NULL,
    START_TIME       TIMESTAMP DEFAULT NULL,
    END_TIME         TIMESTAMP DEFAULT NULL,
    STATUS           VARCHAR(10),
    EXIT_CODE        VARCHAR(2500),
    EXIT_MESSAGE     VARCHAR(2500),
    LAST_UPDATED     TIMESTAMP,
    constraint JOB_INST_EXEC_FK foreign key (JOB_INSTANCE_ID)
        references BATCH_JOB_INSTANCE (JOB_INSTANCE_ID)
);

CREATE TABLE BATCH_JOB_EXECUTION_PARAMS
(
    JOB_EXECUTION_ID BIGINT       NOT NULL,
    PARAMETER_NAME   VARCHAR(100) NOT NULL,
    PARAMETER_TYPE   VARCHAR(100) NOT NULL,
    PARAMETER_VALUE  VARCHAR(2500),
    IDENTIFYING      CHAR(1)      NOT NULL,
    constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID)
        references BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
);

CREATE TABLE BATCH_STEP_EXECUTION
(
    STEP_EXECUTION_ID  BIGINT       NOT NULL PRIMARY KEY,
    VERSION            BIGINT       NOT NULL,
    STEP_NAME          VARCHAR(100) NOT NULL,
    JOB_EXECUTION_ID   BIGINT       NOT NULL,
    CREATE_TIME        TIMESTAMP    NOT NULL,
    START_TIME         TIMESTAMP DEFAULT NULL,
    END_TIME           TIMESTAMP DEFAULT NULL,
    STATUS             VARCHAR(10),
    COMMIT_COUNT       BIGINT,
    READ_COUNT         BIGINT,
    FILTER_COUNT       BIGINT,
    WRITE_COUNT        BIGINT,
    READ_SKIP_COUNT    BIGINT,
    WRITE_SKIP_COUNT   BIGINT,
    PROCESS_SKIP_COUNT BIGINT,
    ROLLBACK_COUNT     BIGINT,
    EXIT_CODE          VARCHAR(2500),
    EXIT_MESSAGE       VARCHAR(2500),
    LAST_UPDATED       TIMESTAMP,
    constraint JOB_EXEC_STEP_FK foreign key (JOB_EXECUTION_ID)
        references BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
);

CREATE TABLE BATCH_STEP_EXECUTION_CONTEXT
(
    STEP_EXECUTION_ID  BIGINT        NOT NULL PRIMARY KEY,
    SHORT_CONTEXT      VARCHAR(2500) NOT NULL,
    SERIALIZED_CONTEXT TEXT,
    constraint STEP_EXEC_CTX_FK foreign key (STEP_EXECUTION_ID)
        references BATCH_STEP_EXECUTION (STEP_EXECUTION_ID)
);

CREATE TABLE BATCH_JOB_EXECUTION_CONTEXT
(
    JOB_EXECUTION_ID   BIGINT        NOT NULL PRIMARY KEY,
    SHORT_CONTEXT      VARCHAR(2500) NOT NULL,
    SERIALIZED_CONTEXT TEXT,
    constraint JOB_EXEC_CTX_FK foreign key (JOB_EXECUTION_ID)
        references BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
);

CREATE SEQUENCE BATCH_STEP_EXECUTION_SEQ MAXVALUE 9223372036854775807 NO CYCLE;
CREATE SEQUENCE BATCH_JOB_EXECUTION_SEQ MAXVALUE 9223372036854775807 NO CYCLE;
CREATE SEQUENCE BATCH_JOB_SEQ MAXVALUE 9223372036854775807 NO CYCLE;
