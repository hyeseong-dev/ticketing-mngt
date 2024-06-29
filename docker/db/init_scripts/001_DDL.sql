
DROP DATABASE IF EXISTS ticketing;
CREATE DATABASE ticketing CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ticketing;
-- 사용자 테이블
CREATE TABLE user (
                       user_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '사용자 ID (기본 키)',
                       email VARCHAR(30) UNIQUE NOT NULL COMMENT '사용자 이메일',
                       password VARCHAR(255) NOT NULL COMMENT '사용자 비밀번호',
                       name VARCHAR(100) NOT NULL COMMENT '사용자 이름',
                       role ENUM('ADMIN', 'USER') NOT NULL DEFAULT 'USER' COMMENT '사용자 역할',
                       balance DECIMAL(10, 2) DEFAULT 0 COMMENT '사용자 잔액',
                       address VARCHAR(100) COMMENT '사용자 주소',
                       phone_number VARCHAR(20) UNIQUE COMMENT '사용자 전화번호',
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '사용자 생성 일시',
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '사용자 정보 수정 일시',
                       deleted_at TIMESTAMP COMMENT '사용자 삭제 일시',
                       email_verified BOOLEAN DEFAULT FALSE COMMENT '이메일 인증 여부'
) COMMENT '사용자 정보를 저장하는 테이블';


-- 리프레시 토큰 테이블
CREATE TABLE refresh_token (
                               token_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '리프레시 토큰 고유 ID',
                               user_id BIGINT NOT NULL COMMENT '사용자 고유 ID (외래키)',
                               token VARCHAR(255) NOT NULL COMMENT '리프레시 토큰 값',
                               ip VARCHAR(20) NOT NULL COMMENT 'IP주소(IPv4, IPv6)',
                               device_info VARCHAR(255) NULL COMMENT '디바이스 정보(예: User-Agent Info on HTTP REQUEST)',
                               expiry_date TIMESTAMP NOT NULL COMMENT '리프레시 토큰 만료 일시',
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '리프레시 토큰 생성 일시',
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '리프레시 토큰 정보 수정 일시',
                               CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES user (user_id) -- 수정: id -> user_id
) COMMENT '사용자의 리프레시 토큰을 저장하는 테이블';

-- 장소 테이블
CREATE TABLE place (
                       place_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '공연장 ID (기본 키)',
                       name VARCHAR(100) NOT NULL COMMENT '공연장 이름',
                       seats_cnt INT NOT NULL DEFAULT 0 COMMENT '좌석 개수',
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                       updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
                       PRIMARY KEY (place_id)
) COMMENT '공연장 정보를 저장하는 테이블';

-- 콘서트 테이블
CREATE TABLE concert (
                         concert_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '콘서트 ID (기본 키)',
                         place_id BIGINT NOT NULL COMMENT '공연장 ID (외래키)',
                         name VARCHAR(50) NOT NULL COMMENT '콘서트 이름',
                         created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                         updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
                         PRIMARY KEY (concert_id),
                         FOREIGN KEY (place_id) REFERENCES place (place_id)
) COMMENT '콘서트 정보를 저장하는 테이블';

-- -- 콘서트 날짜 테이블
-- CREATE TABLE concert_date (
--                               concert_date_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '콘서트 날짜 ID (기본 키)',
--                               concert_id BIGINT NOT NULL COMMENT '콘서트 ID (외래키)', // 추후 복원
-- --                               place_id BIGINT NOT NULL COMMENT '공연장 ID (외래키)',// 추후 복원
--                               concert_date DATETIME NOT NULL COMMENT '콘서트 날짜',
--                               created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
--                               updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
--                               PRIMARY KEY (concert_date_id),
--                               FOREIGN KEY (concert_id) REFERENCES concert (concert_id)
-- --                               FOREIGN KEY (place_id) REFERENCES place (place_id)
-- ) COMMENT '콘서트 날짜 정보를 저장하는 테이블';

-- 콘서트 날짜 테이블
CREATE TABLE concert_date (
                              concert_date_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '콘서트 날짜 ID (기본 키)',
                              concert_id BIGINT NOT NULL COMMENT '콘서트 ID (외래키)',
--                               place_id BIGINT NOT NULL COMMENT '공연장 ID (외래키)',// 추후 복원
                              concert_date DATETIME NOT NULL COMMENT '콘서트 날짜',
                              created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                              updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
                              PRIMARY KEY (concert_date_id),
                              FOREIGN KEY (concert_id) REFERENCES concert (concert_id)
--                               FOREIGN KEY (place_id) REFERENCES place (place_id)
) COMMENT '콘서트 날짜 정보를 저장하는 테이블';

-- 좌석 테이블
CREATE TABLE seat (
                      seat_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '좌석 ID (기본 키)',
                      concert_date_id BIGINT NOT NULL COMMENT '콘서트 날짜 ID (외래키)',
                      place_id BIGINT NOT NULL COMMENT '공연장 ID(외래키)',
                      seat_num INT NOT NULL COMMENT '좌석 번호',
                      price DECIMAL(10, 2) NOT NULL COMMENT '가격',
                      status ENUM('AVAILABLE', 'DISABLE') NOT NULL COMMENT '예약 상태',
                      created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                      updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
                      PRIMARY KEY (seat_id),
                      FOREIGN KEY (place_id) REFERENCES place (place_id),
                      FOREIGN KEY (concert_date_id) REFERENCES concert_date (concert_date_id)
) COMMENT '좌석 정보를 저장하는 테이블';

-- 예약 테이블
CREATE TABLE reservation (
                             reservation_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '예약 ID (기본 키)',
                             status ENUM('ING', 'RESERVED', 'CANCEL') NOT NULL COMMENT '예약 상태',
                             user_id BIGINT NOT NULL COMMENT '사용자 ID (외래키)',
                             concert_id BIGINT NOT NULL COMMENT '콘서트 ID(외래키)',
                             concert_date_id BIGINT NOT NULL COMMENT '콘서트 날짜 ID (외래키)',
                             seat_id BIGINT NOT NULL COMMENT '좌석 ID (외래키)',
                             reserved_at DATETIME NOT NULL COMMENT '예약 일시',
                             created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                             updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
                             PRIMARY KEY (reservation_id),
                             FOREIGN KEY (user_id) REFERENCES user (user_id),
                             FOREIGN KEY (concert_date_id) REFERENCES concert_date (concert_date_id),
                             FOREIGN KEY (concert_id) REFERENCES concert(concert_id),
                             FOREIGN KEY (seat_id) REFERENCES seat (seat_id)
) COMMENT '예약 정보를 저장하는 테이블';

-- 결제 테이블
CREATE TABLE payment (
                         payment_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '결제 ID (기본 키)',
                         reservation_id BIGINT NOT NULL COMMENT '예약 ID (외래키)',
                         price DECIMAL(10, 2) NOT NULL COMMENT '결제 금액',
                         status ENUM('READY', 'COMPLETE', 'CANCEL', 'REFUND') NOT NULL COMMENT '결제 상태',
                         paid_at DATETIME NOT NULL COMMENT '결제 일시',
                         created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                         updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
                         PRIMARY KEY (payment_id),
                         FOREIGN KEY (reservation_id) REFERENCES reservation (reservation_id)
) COMMENT '결제 정보를 저장하는 테이블';

-- 대기열 테이블
CREATE TABLE waiting_queue (
                               waiting_queue_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '대기열 ID (기본 키)',
                               user_id BIGINT NOT NULL COMMENT '사용자 ID (외래키)',
                               token VARCHAR(255) NOT NULL COMMENT '대기열 토큰',
                               status ENUM('EXPIRED', 'ACTIVE', 'WAITING') NOT NULL COMMENT '대기 상태',
                               created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                               updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
                               PRIMARY KEY (waiting_queue_id),
                               FOREIGN KEY (user_id) REFERENCES user (user_id) -- 수정: id -> user_id
) COMMENT '대기열 정보를 저장하는 테이블';