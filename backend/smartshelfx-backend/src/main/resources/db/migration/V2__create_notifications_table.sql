-- Create notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    message VARCHAR(500) NOT NULL,
    user_email VARCHAR(255),
    user_role VARCHAR(50),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    priority VARCHAR(50) NOT NULL DEFAULT 'NORMAL',
    related_entity_type VARCHAR(50),
    related_entity_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    INDEX idx_user_email (user_email),
    INDEX idx_user_role (user_role),
    INDEX idx_is_read (is_read),
    INDEX idx_type (type),
    INDEX idx_created_at (created_at)
);

-- Create index for efficient queries
CREATE INDEX idx_notifications_user_read ON notifications(user_email, user_role, is_read, created_at);
