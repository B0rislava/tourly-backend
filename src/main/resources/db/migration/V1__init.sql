-- V1__init.sql
-- Create initial schema based on JPA entities

-- 1. users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    profile_picture_url VARCHAR(255),
    bio VARCHAR(1000),
    rating DOUBLE PRECISION DEFAULT 0.0,
    reviews_count INTEGER DEFAULT 0,
    follower_count INTEGER DEFAULT 0,
    certifications VARCHAR(1000),
    tours_completed INTEGER DEFAULT 0,
    is_verified BOOLEAN DEFAULT FALSE NOT NULL
);

-- 2. tours table
CREATE TABLE tours (
    id BIGSERIAL PRIMARY KEY,
    guide_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    location VARCHAR(255) NOT NULL,
    duration VARCHAR(255) NOT NULL,
    max_group_size INTEGER NOT NULL,
    available_spots INTEGER DEFAULT 0 NOT NULL,
    price_per_person DOUBLE PRECISION NOT NULL,
    whats_included VARCHAR(1000),
    scheduled_date DATE,
    start_time TIME WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    status VARCHAR(255) NOT NULL,
    rating DOUBLE PRECISION DEFAULT 0.0,
    reviews_count INTEGER DEFAULT 0,
    meeting_point VARCHAR(255),
    image_url VARCHAR(255),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    CONSTRAINT fk_tours_guide FOREIGN KEY (guide_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. saved_tours table (many-to-many join table)
CREATE TABLE saved_tours (
    user_id BIGINT NOT NULL,
    tour_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, tour_id),
    CONSTRAINT fk_saved_tours_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_saved_tours_tour FOREIGN KEY (tour_id) REFERENCES tours(id) ON DELETE CASCADE
);

-- 4. tags table
CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    is_system BOOLEAN DEFAULT TRUE NOT NULL
);

-- 5. tour_tags table (many-to-many join table)
CREATE TABLE tour_tags (
    tour_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (tour_id, tag_id),
    CONSTRAINT fk_tour_tags_tour FOREIGN KEY (tour_id) REFERENCES tours(id) ON DELETE CASCADE,
    CONSTRAINT fk_tour_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

-- 6. bookings table
CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    tour_id BIGINT NOT NULL,
    number_of_participants INTEGER DEFAULT 1 NOT NULL,
    booking_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    status VARCHAR(255) NOT NULL,
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_bookings_tour FOREIGN KEY (tour_id) REFERENCES tours(id) ON DELETE CASCADE
);

-- 7. reviews table
CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    reviewer_id BIGINT NOT NULL,
    guide_id BIGINT NOT NULL,
    tour_id BIGINT NOT NULL,
    booking_id BIGINT NOT NULL UNIQUE,
    tour_rating INTEGER NOT NULL,
    guide_rating INTEGER NOT NULL,
    comment VARCHAR(2000),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT fk_reviews_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_guide FOREIGN KEY (guide_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_tour FOREIGN KEY (tour_id) REFERENCES tours(id) ON DELETE CASCADE
);

-- 8. follows table
CREATE TABLE follows (
    id BIGSERIAL PRIMARY KEY,
    follower_id BIGINT NOT NULL,
    following_id BIGINT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT unique_follower_following UNIQUE (follower_id, following_id),
    CONSTRAINT fk_follows_follower FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_follows_following FOREIGN KEY (following_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 9. messages table
CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    tour_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content VARCHAR(1000) NOT NULL,
    timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT fk_messages_tour FOREIGN KEY (tour_id) REFERENCES tours(id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 10. notifications table
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    type VARCHAR(255),
    related_id BIGINT,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 11. refresh_tokens table
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(500) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 12. verification_tokens table
CREATE TABLE verification_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT fk_verification_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
