# 콘서트 좌석 예매

# [항해 취업 리부트 백엔드 3기] 서버 구축

### *시나리오 - 콘서트 좌석 예매 서비스*

### 목차

- [1. 요구사항 분석](##one)
- [2. 동시성 제어 처리 로직 비교 구현](https://iwannabarmus.tistory.com/30)
- [3. DB Index 사용과 비교](https://iwannabarmus.tistory.com/36)
- [4. 대기열 설계 및 구현]()
- [5. Transaction 범위와 책임 분리 방안 설계](https://iwannabarmus.tistory.com/38)
- [6. 좌석 예약 정보를 데이터 플랫폼으로 전송 로직 구현](https://iwannabarmus.tistory.com/39)
- [7. 부하테스트 & 장애 대응](https://iwannabarmus.tistory.com/41)
- [Trouble Shooting](##4.-Trouble-Shooting)
- [개발하면서 끄적](##5.-개발하면서-끄적)
- [기술 스택](##6.-기술-스택)

---
<h2 id="one">1. 요구사항 분석</h2>

### 프로젝트 Milestone & 요구사항 명세서 & API 명세서

[구글닥스 문서 바로가기](https://docs.google.com/spreadsheets/d/17yUn-cEa9uq2jE7_bTpjfXoeaXIAGt_91FmHhMm2BJo/edit?gid=0#gid=0)

### POSTMAN UI

[POSTMAN UI](https://documenter.getpostman.com/view/14042841/2sA3kSoPGR)

### UML 다이어그램

*변경된 로직이 있어 추후 수정 예정입니다.*
![uml.png](image/uml.png)

### ERD 명세

```mermaid
erDiagram
    users {
        BIGINT user_id PK "사용자 ID (기본 키)"
        VARCHAR email "사용자 이메일"
        VARCHAR password "사용자 비밀번호"
        VARCHAR name "사용자 이름"
        ENUM role "사용자 역할"
        DECIMAL balance "사용자 잔액"
        VARCHAR address "사용자 주소"
        VARCHAR phone_number "사용자 전화번호"
        TIMESTAMP created_at "사용자 생성 일시"
        TIMESTAMP updated_at "사용자 정보 수정 일시"
        TIMESTAMP deleted_at "사용자 삭제 일시"
        BOOLEAN email_verified "이메일 인증 여부"
    }

    reservation {
        BIGINT reservation_id PK "예약 ID (기본 키)"
        ENUM status "예약 상태"
        BIGINT user_id FK "사용자 ID (외래키)"
        BIGINT concert_id FK "콘서트 ID(외래키)"
        BIGINT concert_date_id FK "콘서트 날짜 ID (외래키)"
        BIGINT seat_id FK "좌석 ID (외래키)"
        DECIMAL price "예약 가격 정보"
        DATETIME reserved_at "예약 일시"
        DATETIME created_at "생성 일시"
        DATETIME updated_at "수정 일시"
    }

    place {
        BIGINT place_id PK "공연장 ID (기본 키)"
        VARCHAR name "공연장 이름"
        INT seats_cnt "좌석 개수"
        DATETIME created_at "생성 일시"
        DATETIME updated_at "수정 일시"
    }

    concert {
        BIGINT concert_id PK "콘서트 ID (기본 키)"
        BIGINT place_id FK "공연장 ID (외래키)"
        VARCHAR name "콘서트 이름"
        DATETIME created_at "생성 일시"
        DATETIME updated_at "수정 일시"
    }

    concert_date {
        BIGINT concert_date_id PK "콘서트 날짜 ID (기본 키)"
        BIGINT concert_id FK "콘서트 ID (외래키)"
        DATETIME concert_date "콘서트 날짜"
        DATETIME created_at "생성 일시"
        DATETIME updated_at "수정 일시"
    }

    seat {
        BIGINT seat_id PK "좌석 ID (기본 키)"
        BIGINT concert_date_id FK "콘서트 날짜 ID (외래키)"
        BIGINT place_id FK "공연장 ID(외래키)"
        INT seat_num "좌석 번호"
        DECIMAL price "가격"
        ENUM status "예약 상태"
        DATETIME created_at "생성 일시"
        DATETIME updated_at "수정 일시"
    }

    inventory {
        BIGINT inventory_id PK "기본 키 (자동 증가)"
        BIGINT concert_id FK "관련된 콘서트 ID"
        BIGINT concert_date_id FK "관련된 콘서트 날짜 ID"
        BIGINT total "총 좌석 수 (기본값: 0)"
        BIGINT remaining "남은 좌석 수 (기본값: 0)"
        BIGINT version "버전 관리 (기본값: 0)"
        DATETIME created_at "생성 일시 (기본값: 현재 시간)"
        DATETIME updated_at "수정 일시 (업데이트 시 현재 시간)"
    }

    payment {
        BIGINT payment_id PK "결제 ID (기본 키)"
        BIGINT user_id FK "유저 ID (외래키)"
        BIGINT reservation_id FK "예약 ID (외래키)"
        DECIMAL price "결제 금액"
        ENUM status "결제 상태"
        DATETIME paid_at "결제 일시"
        DATETIME created_at "생성 일시"
        DATETIME updated_at "수정 일시"
    }

    users ||--o{ reservation : "has many"
    reservation ||--o| users : "belongs to"
    reservation ||--o| concert : "belongs to"
    reservation ||--o| concert_date : "belongs to"
    reservation ||--o| seat : "belongs to"
    reservation ||--o| payment : "includes"

    place ||--o{ concert : "has many"
    concert ||--o| place : "belongs to"
    concert ||--o{ concert_date : "has many"
    concert_date ||--o| concert : "belongs to"
    concert_date ||--o{ seat : "has many"
    seat ||--o| concert_date : "belongs to"
    seat ||--o| place : "belongs to"
    concert_date ||--o{ inventory : "has many"
    concert ||--o{ inventory : "has many"
    inventory ||--o| concert_date : "belongs to"
    inventory ||--o| concert : "belongs to"
    payment ||--o| users : "belongs to"
    payment ||--o| reservation : "belongs to"

```

---

### Dummy data - postman 호출

- API: http://localhost:8080/concerts/
  ![postman_getConcerts.PNG](image/postman_getConcerts.PNG)

---

## 2. 동시성 제어 처리 로직 비교 구현

[콘서트 예약 서비스에서 발생할 수 있는 동시성 이슈와 처리 방안](https://iwannabarmus.tistory.com/30)

---

## 3. DB Index 사용과 비교

[성능 향상을 위한 DB Index 사용과 비교](https://iwannabarmus.tistory.com/36)

---

## 4. 대기열 설계 및 구현

0. 유즈 케이스 설정

- 콘서트 조회, 콘서트 날짜 조회 api는 대기열 X
- 콘서트 날짜를 선택하여 [예매하기] 버튼을 눌러 콘서트 좌석 조회 api 호출하는데,
  해당 좌석 조회 api에 대기열을 붙인다.

1. api 목록

- 대기열 토큰 활성여부 조회
    - GET /token
        - request: token
        - response : token, isActive, waitingInfo(대기순서, 잔여 시간)
        - 토큰 request가 없으면 새로 생성하여 응답 반환
        - 반환된 isActive값이 true일 때까지 클라이언트에서 5초마다 polling 방식으로 호출하며,
          반환된 isActive값이 true면 페이지에 진입한다.
- 페이지에 진입시, Header에 token을 넣어 관리한다.

2. 대기열 토큰 구현

- 대기열 토큰은 Redis를 활용하여 관리한다.
- API 당 대기열 기능을 붙일 수 있다.
- 해당 API 호출의 작업이 완료되거나 기한이 만료된 토큰은 삭제로 관리한다.
- 대기열 토큰은 두 가지 상태를 가진다.
    - waiting
        - sorted set 자료구조로 저장 (key: WAIT_KEY/ score: 요청시간/ member: token)
    - active
        - sorted set 자료구조로 저장 (key: ACTIVE_KEY/ score: 요청시간/ member: token)
        - 작업이 완료되거나 만료 일시가 지나면 삭제 처리

3. Active token 전환 방식

- N초마다 M개의 토큰을 활성 토큰으로 전환하는 방식 선택
    - 대기열이 필요한 이유는 대규모 트래픽이 한번에 서비스로 유입되는 것을 방지하기 위함이므로,
      정확한 유저 수를 일정하게 유지하기보다는 사용자에게 제공한 대기 잔여 시간을 일관성 있게 보장하여
      사용자의 경험에 대한 만족도를 주는 것이 서비스 목적에 더 적합하다고 판단하였다.

4. 동시 접속자와 대기열 잔여 시간 계산 방식

- 한 유저가 콘서트 예약 사이클을 완료하는 데 걸리는 예상 시간
    - 1분
- DB에 동시에 접근할 수 있는 트래픽의 최대치를 계산
    - 약 100 TPS(초당 트랜잭션 수) ⇒ 1분당 6,000
- 콘서트 예약 사이클 동안 호출하는 api
    - 2(콘서트 좌석 조회 api, 좌석 예약 api) * 1.5(동시성 이슈에 의해 예약에 실패하는 케이스를 위한 재시도 계수(예측치)) = 3
- 분당 처리할 수 있는 동시접속자 수 = 2,000명
    - 10초마다 200명씩 유효한 토큰으로 전환
    - 전환되는 인원수로 대기열 순번 계산

---

## 5. Transaction 범위와 책임 분리 방안 설계

[Transaction 범위와 책임 분리 방안 설계](https://iwannabarmus.tistory.com/38)

---

## 6. 좌석 예약 정보를 데이터 플랫폼으로 전송 로직 구현

[콘서트 좌석 예약 정보를 데이터 플랫폼으로 전달한다면?](https://iwannabarmus.tistory.com/39)

---

## 7. 부하테스트 & 장애 대응

[k6로 부하테스트 해보기](https://iwannabarmus.tistory.com/41)

---

## 작업 내용

1주차

- ~2024.04.02
    - 요구사항 분석
    - 프로젝트 명세 문서 작성
    - ERD 설계
    - 프로젝트 세팅
- 2024.04.03
    - 아키텍처 구조 설계
    - Mock API 작성 중
- 2024.04.04
    - ERD 재설계
    - Mock API 작성
    - Dummy Data 반환 및 http 테스트
    - SecurityConfiguration 작성 (postman 테스트용)

2주차

- ~2024.04.09
    - swagger UI
    - 기본 api 명세 인터페이스 구현
- 2024.04.10
    - 기본 기능 api 서비스 로직 구현
    - 단위테스트 작성
- 2024.04.11
    - 대기열 서비스 로직 구현
    - 단위테스트 작성

3주차

- ~2024.04.18
    - 테이블 재설계: 도메인 어그리게이트 별로 분리하여 재설계
    - 통합테스트 작성
    - 대기열 기능 고도화

4주차

- 2024.04.24
    - 브랜치 전략 수립

5주차

- 2024.05.02
    - 동시성 제어 처리 방식 구현, 비교 정리

6주차

- 2024.05.09
    - Index 사용과 성능 비교
    - 대기열 설계, 구현 (Redis)

---

## Trouble Shooting

*모든 과정을 기록할 수는 없었지만, 5주차 진행 과정에서의 나의 트러블 슈팅 과정을 기록하였다.*

[Trouble Shooting 기록 바로가기](https://iwannabarmus.tistory.com/27)





---

## 개발하면서 끄적

### 3주차 인사이트

- 요구사항을 api 명세로 녹여내고, 기능 구현 흐름을 작성해보는 것
- DB를 적절한 정규화 방식으로 설계하는 것
- 테이블도 결국 캡슐화? 의존성을 줄여..

### 4주차 인사이트

- 대기열 구현 시 생각해볼 것들

### 5주차 인사이트

- jpa 설계 원칙대로 객체지향을 살리면서 객체로 연관짓는 것과, 유연성을 살리고 강결합을 막으며 pk만 들고 있는 것의 차이
    - 도메인 애그리거트? 애그리거트 루트?
    - 도메인 애그리거트를 잘 생각해서 의존성을 분리시키자
- 결국 서비스는 핵심 기능이 얼마나 잘 돌아가는지가 중요하다.
    - 핵심 기능이 본인의 역할의 책임을 다 할 수 있도록 도메인 기능을 잘 분리하여 설계하자.
    - 모든 것을 다 갖춘 설계는 없다. 각 장단점이 있다. 내가 해야 할 것은 어디에 집중할 지 스스로 선택하는 것
    - 여러 가지를 모두 구현해보며 나만의 기준과 나의 스타일을 찾자.

### 7주차 인사이드

- 동시성 처리하며, 역시 케이스 바이 케이스

### 8주차 인사이트

- 인덱스 사용도 상황을 고려해서
- 인덱스 좋다.

---

## 기술 스택

- Spring boot
- Jpa
- Mysql

## 테스트 시나리오

- 테스트 코드 주석 참고
