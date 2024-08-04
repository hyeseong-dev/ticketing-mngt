
DROP DATABASE IF EXISTS ticketing_db;
CREATE DATABASE ticketing_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ticketing_db;

-- 예약 테이블
CREATE TABLE reservation (
                             reservation_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '예약 ID (기본 키)',
                             status ENUM('ING', 'RESERVED', 'CANCEL') NOT NULL COMMENT '예약 상태',
                             user_id BIGINT NOT NULL COMMENT '사용자 ID (외래키)',
                             concert_id BIGINT NOT NULL COMMENT '콘서트 ID(외래키)',
                             concert_date_id BIGINT NOT NULL COMMENT '콘서트 날짜 ID (외래키)',
                             seat_id BIGINT NOT NULL COMMENT '좌석 ID (외래키)',
                             price DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '예약 가격 정보',
                             reserved_at DATETIME NOT NULL COMMENT '예약 일시',
                             created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                             updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
                             expires_at DATETIME DEFAULT NULL COMMENT '만료 일시',
                             PRIMARY KEY (reservation_id),
                             INDEX idx_user_id (user_id),
                             INDEX idx_concert_id (concert_id),
                             INDEX idx_concert_date_id (concert_date_id),
                             INDEX idx_seat_id (seat_id),
                             INDEX idx_reserved_at (reserved_at)
) COMMENT '예약 정보를 저장하는 테이블';

CREATE USER 'developer'@'%' IDENTIFIED WITH mysql_native_password BY '12345';
GRANT ALL PRIVILEGES ON ticketing_db.* TO 'developer'@'%';

CREATE USER 'developer'@'localhost' IDENTIFIED WITH mysql_native_password BY '12345';
GRANT ALL PRIVILEGES ON ticketing_db.* TO 'developer'@'localhost';

CREATE USER 'developer'@'_gateway' IDENTIFIED WITH mysql_native_password BY '12345';
GRANT ALL PRIVILEGES ON ticketing_db.* TO 'developer'@'_gateway';

FLUSH PRIVILEGES;



