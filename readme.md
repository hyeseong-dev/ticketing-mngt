# 콘서트 티켓 좌석 예매

### *시나리오 - 콘서트 좌석 예매 서비스*

### 목차

- [1. 요구사항 분석](##one)
- [2. 동시성 제어 처리 로직 비교 구현](https://iwannabarmus.tistory.com/30)
- [3. DB Index 사용과 비교](https://iwannabarmus.tistory.com/36)
- [4. 대기열 설계 및 구현]()
- [5. Transaction 범위와 책임 분리 방안 설계](https://iwannabarmus.tistory.com/38)
- [6. 부하테스트 & 장애 대응](https://iwannabarmus.tistory.com/41)
- [Trouble Shooting](##4.-Trouble-Shooting)
- [주차별 인사이트](##5.-개발하면서-끄적)
- [기술 스택](##6.-기술-스택)

---
<h2 id="one">1. 요구사항 분석</h2>

### 프로젝트 Milestone & 요구사항 명세서 & API 명세서

[구글닥스 문서 바로가기](https://docs.google.com/spreadsheets/d/17yUn-cEa9uq2jE7_bTpjfXoeaXIAGt_91FmHhMm2BJo/edit?gid=0#gid=0)

### POSTMAN UI

[POSTMAN UI](https://documenter.getpostman.com/view/14042841/2sA3kSoPGR)

### 시스템 아키텍처

```mermaid
C4Container
    title 이벤트 기반 아키텍처 - 마이크로서비스 티켓팅 시스템

    Container_Boundary(gateway, "API 계층") {
        Container(api_gateway, "API 게이트웨이", "Spring Cloud Gateway", "요청 라우팅 및 인증")
    }

    Container_Boundary(discovery, "서비스 디스커버리") {
        Container(eureka, "유레카 서버", "Spring Cloud Netflix", "서비스 등록 및 발견")
    }

    Container_Boundary(message_broker, "메시지 브로커") {
        Container(kafka, "카프카", "Apache Kafka", "이벤트 스트리밍 플랫폼")
    }

    Container_Boundary(cache, "캐싱 계층") {
        Container(redis, "레디스", "인메모리 데이터 저장소", "캐싱 및 세션 관리")
    }

    Container_Boundary(services, "마이크로서비스 계층") {
        Container(service1, "사용자 서비스", "Spring Boot", "사용자 관리")
        Container(service2, "예약 서비스", "Spring Boot", "티켓 예약")
        Container(service3, "콘서트 서비스", "Spring Boot", "콘서트 정보")
        Container(service4, "결제 서비스", "Spring Boot", "결제 처리")
    }

    Container_Boundary(databases, "데이터베이스 계층") {
        ContainerDb(db1, "사용자 DB", "MySQL", "사용자 데이터")
        ContainerDb(db2, "예약 DB", "MySQL", "예약 데이터")
        ContainerDb(db3, "콘서트 DB", "MySQL", "콘서트 데이터")
        ContainerDb(db4, "결제 DB", "MySQL", "결제 데이터")
    }

    Rel(api_gateway, service1, "요청 라우팅", "HTTP/REST")
    Rel(api_gateway, service2, "요청 라우팅", "HTTP/REST")
    Rel(api_gateway, service3, "요청 라우팅", "HTTP/REST")
    Rel(api_gateway, service4, "요청 라우팅", "HTTP/REST")

    Rel(service1, eureka, "서비스 등록", "HTTP")
    Rel(service2, eureka, "서비스 등록", "HTTP")
    Rel(service3, eureka, "서비스 등록", "HTTP")
    Rel(service4, eureka, "서비스 등록", "HTTP")

    Rel(service1, kafka, "이벤트 발행/구독", "Kafka 프로토콜")
    Rel(service2, kafka, "이벤트 발행/구독", "Kafka 프로토콜")
    Rel(service3, kafka, "이벤트 발행/구독", "Kafka 프로토콜")
    Rel(service4, kafka, "이벤트 발행/구독", "Kafka 프로토콜")

    Rel(api_gateway, redis, "캐싱", "Redis 프로토콜")
    Rel(service1, redis, "캐싱", "Redis 프로토콜")
    Rel(service2, redis, "캐싱", "Redis 프로토콜")
    Rel(service3, redis, "캐싱", "Redis 프로토콜")
    Rel(service4, redis, "캐싱", "Redis 프로토콜")

    Rel(service1, db1, "읽기/쓰기", "JDBC")
    Rel(service2, db2, "읽기/쓰기", "JDBC")
    Rel(service3, db3, "읽기/쓰기", "JDBC")
    Rel(service4, db4, "읽기/쓰기", "JDBC")

    Rel(eureka, api_gateway, "서비스 레지스트리 제공", "HTTP")

    UpdateLayoutConfig($c4ShapeInRow="4", $c4BoundaryInRow="1")
```

### UML 다이어그램

<details>
<summary>인증&인가 프로세스</summary>
<div markdown="1">

```mermaid
sequenceDiagram
    actor Client
    participant Gateway as API Gateway
    participant UserService as User Service
    participant Redis
    participant DB as Database

    %% 로그인 프로세스
    Client->>Gateway: POST /api/auth/login
    Gateway->>UserService: 로그인 요청 전달
    UserService->>DB: 사용자 정보 조회
    DB-->>UserService: 사용자 정보 반환
    UserService->>UserService: 요청 데이터 유효성 검증
    UserService->>UserService: JWT 토큰 생성 (Access + Refresh)
    UserService->>Redis: Refresh 토큰 저장 (7일 유효)
    UserService-->>Gateway: 로그인 응답 (토큰 포함)
    Gateway-->>Client: 로그인 성공 응답

    %% 보호된 리소스 접근
    Client->>Gateway: 보호된 리소스 요청 (AccessToken 포함)
    Gateway->>Gateway: AccessToken 검증
    alt AccessToken 유효
        Gateway->>UserService: 요청 전달 (User-Id, User-Role 헤더 추가)
        UserService-->>Gateway: 응답
        Gateway-->>Client: 응답 전달
    else AccessToken 만료
        Gateway-->>Client: 401 Unauthorized
        Client->>Gateway: POST /api/auth/refresh (RefreshToken 포함)
        Gateway->>UserService: Refresh 토큰 검증 요청
        UserService->>Redis: RefreshToken 유효성 확인
        Redis-->>UserService: RefreshToken 상태 반환
        UserService->>UserService: 새 AccessToken 생성
        UserService->>Redis: RefreshToken 업데이트
        UserService-->>Gateway: 새 AccessToken 반환
        Gateway-->>Client: 새 AccessToken 전달
    end

    %% 로그아웃 프로세스
    Client->>Gateway: GET /api/auth/logout (AccessToken 포함)
    Gateway->>UserService: 로그아웃 요청 전달
    UserService->>Redis: RefreshToken 삭제
    UserService->>Redis: AccessToken 블랙리스트에 추가
    UserService-->>Gateway: 로그아웃 성공 응답
    Gateway-->>Client: 로그아웃 완료 응답

```

</div>
</details>

<details>
<summary>콘서트 서비스 프로세스</summary>
<div markdown="2">

```mermaid
sequenceDiagram
    actor User as 사용자
    participant CC as 콘서트컨트롤러
    participant CS as 콘서트서비스
    participant SS as 좌석서비스
    participant CR as 콘서트저장소
    participant SR as 좌석저장소
    participant Redis
    participant MySQL

    User->>CC: 콘서트 목록 요청
    CC->>CS: 모든 콘서트 조회 요청
    CS->>CR: 전체 콘서트 데이터 요청
    CR->>MySQL: 콘서트 테이블에서 모든 데이터 조회
    MySQL-->>CR: 콘서트 데이터 반환
    CR-->>CS: 콘서트 목록 반환
    CS-->>CC: 콘서트 응답 데이터 생성
    CC-->>User: 콘서트 목록 응답

    User->>CC: 특정 콘서트 정보 요청
    CC->>CS: 콘서트 상세 정보 조회 요청
    CS->>CR: 특정 콘서트 ID로 데이터 요청
    CR->>MySQL: 해당 ID의 콘서트 데이터 조회
    MySQL-->>CR: 콘서트 데이터 반환
    CR-->>CS: 콘서트 정보 반환
    CS-->>CC: 콘서트 상세 응답 데이터 생성
    CC-->>User: 특정 콘서트 정보 응답

    User->>CC: 콘서트 날짜 정보 요청
    CC->>CS: 콘서트 날짜 조회 요청
    CS->>CR: 콘서트 ID로 데이터 요청
    CR->>MySQL: 해당 ID의 콘서트와 관련 날짜 데이터 조회
    MySQL-->>CR: 콘서트 및 날짜 데이터 반환
    CR-->>CS: 콘서트 (날짜 포함) 정보 반환
    CS->>SR: 각 날짜별 가용 좌석 존재 여부 확인
    SR->>MySQL: 날짜별 사용 가능한 좌석 수 조회
    MySQL-->>SR: 가용 좌석 수 반환
    SR-->>CS: 날짜별 좌석 가용 여부 반환
    CS-->>CC: 날짜 정보 응답 데이터 생성
    CC-->>User: 콘서트 날짜 정보 응답

    User->>CC: 특정 날짜의 가용 좌석 요청
    CC->>SS: 가용 좌석 조회 요청
    SS->>Redis: 캐시된 가용 좌석 정보 요청
    alt 캐시 히트
        Redis-->>SS: 캐시된 가용 좌석 데이터 반환
    else 캐시 미스
        SS->>SR: 데이터베이스에서 가용 좌석 조회 요청
        SR->>MySQL: 특정 날짜의 사용 가능한 좌석 데이터 조회
        MySQL-->>SR: 가용 좌석 데이터 반환
        SR-->>SS: 가용 좌석 목록 반환
        SS->>Redis: 가용 좌석 정보 캐시 저장 (만료 시간 설정)
    end
    SS-->>CC: 가용 좌석 목록 반환
    CC-->>User: 사용 가능한 좌석 목록 응답

    User->>CC: 특정 날짜의 모든 좌석 요청
    CC->>SS: 전체 좌석 조회 요청
    SS->>Redis: 캐시된 전체 좌석 정보 요청
    alt 캐시 히트
        Redis-->>SS: 캐시된 전체 좌석 데이터 반환
    else 캐시 미스
        SS->>SR: 데이터베이스에서 전체 좌석 조회 요청
        SR->>MySQL: 특정 날짜의 모든 좌석 데이터 조회
        MySQL-->>SR: 전체 좌석 데이터 반환
        SR-->>SS: 전체 좌석 목록 반환
        SS->>Redis: 전체 좌석 정보 캐시 저장 (만료 시간 설정)
    end
    SS-->>CC: 전체 좌석 목록 반환
    CC-->>User: 모든 좌석 목록 응답
```

</div>
</details>

<details>
<summary>대기열 프로세스</summary>
<div markdown="3">

```mermaid
sequenceDiagram
    actor 사용자
    participant API as API 게이트웨이
    participant QC as 대기열 컨트롤러
    participant QS as 대기열 서비스
    participant Redis as 레디스
    participant Kafka as 카프카
    participant RC as 예약 컨트롤러
    participant RS as 예약 서비스

    사용자->>API: POST /api/queue (QueueEntryRequest)
    API->>QC: 대기열 예약(사용자ID, 요청)
    QC->>QS: 대기열 진입(사용자ID, 요청)
    QS->>Redis: 대기열에 추가(대기열키, 사용자ID)
    Redis-->>QS: 위치 반환
    QS->>Kafka: 대기열 이벤트 전송 (QUEUE_ENTRY)
    QS-->>QC: 대기열 진입 응답
    QC-->>API: API 결과<대기열 진입 응답>
    API-->>사용자: 대기열 진입 응답

    loop 대기열 처리
        Kafka->>QS: 대기열 처리(콘서트ID, 콘서트날짜ID)
        QS->>Redis: 상위 사용자 조회(대기열키, 처리 batch 크기)
        Redis-->>QS: 상위 사용자 목록
        loop 각 사용자에 대해
            QS->>QS: 예약 페이지 접근 권한 부여(사용자ID, 콘서트ID, 콘서트날짜ID)
            QS->>Redis: 액세스 토큰 설정(토큰키, 액세스토큰, 만료시간)
            QS->>Redis: 시도 횟수 설정(카운트키, 초기 카운트, 만료시간)
            QS->>Kafka: 예약 접근 권한 부여 이벤트 전송
        end
    end

    사용자->>API: POST /api/reservations/token (TokenRequestDTO)
    API->>RC: 토큰 상태 조회(사용자ID, 요청)
    RC->>RS: 토큰 상태 조회(사용자ID, 요청)
    RS->>Redis: 액세스 토큰 조회(토큰키)
    Redis-->>RS: 토큰 반환
    RS->>Redis: 액세스 토큰 TTL 조회(토큰키)
    Redis-->>RS: TTL 초 반환
    RS-->>RC: 토큰 응답 DTO
    RC-->>API: API 결과<토큰 응답 DTO>
    API-->>사용자: 토큰 상태 응답
```

</div>
</details>

<details>
<summary>예매 프로세스</summary>
<div markdown="4">

```mermaid
sequenceDiagram
    actor User as 사용자
    participant API as API Gateway
    participant Reservation as 예약 서비스
    participant Concert as 콘서트 서비스
    participant Redis
    participant MySQL
    participant Kafka

    User->>API: 콘서트, 회차 및 좌석 리스트 요청
    API->>Concert: 좌석 정보 요청
    Concert->>Redis: 좌석 정보 조회 (ALL_SEATS_KEY)
    alt Redis에 데이터 있음
        Redis-->>Concert: 캐시된 좌석 정보 반환
    else Redis에 데이터 없음
        Concert->>MySQL: 좌석 정보 조회
        MySQL-->>Concert: 좌석 정보 반환
        Concert->>Redis: 좌석 정보 캐싱 (ALL_SEATS_KEY)
    end
    Concert-->>API: 좌석 정보 응답
    API-->>User: 좌석 정보 표시

    User->>API: 좌석 선택 요청
    API->>Reservation: 좌석 선택 처리
    Reservation->>Redis: 좌석 상태 확인 (SEAT_KEY_PREFIX)
    alt 좌석 가능
        Reservation->>Redis: 좌석 상태 업데이트 (TEMP_RESERVED)
        Reservation->>Redis: 임시 예약 키 설정 (TEMP_RESERVATION_KEY)
        Reservation->>Redis: 만료 키 설정 (EXPIRY_KEY)
        Reservation->>Kafka: 좌석 TEMP_RESERVED 이벤트 발행
        Kafka->>Concert: 좌석 상태 업데이트 이벤트 수신
        Concert->>MySQL: 좌석 상태 업데이트 (TEMP_RESERVED)
        Concert->>Redis: ALL_SEATS_KEY 업데이트
        Reservation-->>API: 좌석 선택 성공 응답
        API-->>User: 좌석 선택 완료 알림
    else 좌석 불가능
        Reservation-->>API: 좌석 선택 실패 응답
        API-->>User: 다른 좌석 선택 요청
    end

    alt 5분 내 예약 완료
        User->>API: 예약 완료 요청
        API->>Reservation: 예약 처리
        Reservation->>Kafka: 예약 완료 이벤트 발행
        Kafka->>Concert: 예약 완료 이벤트 수신
        Concert->>MySQL: 좌석 상태 업데이트 (예약 완료)
        Concert->>Redis: 좌석 상태 업데이트 (예약 완료, ALL_SEATS_KEY)
        Reservation-->>API: 예약 완료 응답
        API-->>User: 예약 완료 알림
    else 5분 초과
        Redis->>Kafka: TEMP_RESERVED 만료 이벤트 발행
        Kafka->>Concert: TEMP_RESERVED 만료 이벤트 수신
        Concert->>MySQL: 좌석 상태 복원 (AVAILABLE)
        Concert->>Redis: 좌석 상태 복원 (AVAILABLE, ALL_SEATS_KEY)
    end
```

</div>
</details>

### ERD 명세

<details>
<summary>데이터베이스 객체 관계 다이어그램</summary>
<div markdown="1">

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

    users ||--o{ reservation : "has many"
    reservation ||--o| concert : "includes"
    reservation ||--o| concert_date : "includes"
    reservation ||--o| seat : "includes"
    place ||--o{ concert : "has many"
    concert ||--o{ concert_date : "has many"
    concert_date ||--o{ seat : "has many"
    payment ||--o{ users : "has many"
    payment ||--o| reservation : "includes"
    concert ||--o{ inventory : "has many"
    concert_date ||--o{ inventory : "has many"

```

</div>
</details>


---

## 2. 동시성 제어 처리 로직 비교 구현

[콘서트 예약 서비스에서 발생할 수 있는 동시성 이슈와 처리 방안](https://iwannabarmus.tistory.com/30)

---

## 3. DB Index 사용과 비교

[성능 향상을 위한 DB Index 사용과 비교](https://iwannabarmus.tistory.com/36)

---

## 4. 대기열 설계 및 구현

1. 유즈 케이스 설정

* 콘서트 목록 조회, 콘서트 상세 정보 조회, 콘서트 날짜 조회 API는 대기열 적용 제외
* 특정 콘서트 날짜의 좌석 조회 API에 대기열 적용
* 사용자가 콘서트 날짜를 선택하고 '좌석 선택' 버튼을 클릭할 때 대기열 진입

2. API 목록

* 대기열 진입 및 상태 조회
    * POST /api/queue
        * request: QueueEntryRequest (concertId, concertDateId)
        * response: QueueEntryResponse (userId, position, concertId, concertDateId)
        * 새로운 대기열 진입 요청 시 Redis에 사용자 추가 및 대기 순서 반환
* 대기열 상태 조회
    * GET /api/queue/status
        * request: QueueEntryRequest (concertId, concertDateId)
        * response: QueueStatusResponse (userId, concertId, concertDateId, status, position, token)
        * 현재 대기 상태, 순서, 접근 토큰(발급된 경우) 반환
* 토큰 상태 조회
    * POST /api/reservations/token
        * request: TokenRequestDTO (concertId, concertDateId)
        * response: TokenResponseDTO (userId, concertId, concertDateId, status, position, token, expiryTime)
        * 토큰의 유효성, 만료 시간, 대기 상태 정보 반환
* 클라이언트는 QueueStatusResponse의 status가 READY가 될 때까지 주기적으로 상태 조회 API를 호출
* READY 상태가 되면 좌석 조회 페이지로 이동, 이후 요청 시 헤더에 토큰 포함

3. 대기열 및 토큰 구현

* Redis를 사용하여 대기열과 토큰 관리
* 대기열: Sorted Set 사용 (key: queue:{concertId}:{concertDateId}, score: 진입 시간, member: userId)
* 토큰: String 사용 (key: token:{userId}:{concertId}:{concertDateId}, value: 생성된 토큰)
* 토큰 상태:
    * WAITING: 대기열에 있는 상태
    * READY: 접근 권한이 부여된 상태
    * NOT_IN_QUEUE: 대기열에 없는 상태
* 토큰 만료: 발급 후 일정 시간(예: 10분) 경과 시 자동 삭제

4. 대기열 처리 및 토큰 활성화 방식

* 주기적으로(예: 10초마다) 일정 수(예: 100명)의 사용자를 대기열에서 제거하고 접근 토큰 발급
* Kafka를 사용하여 대기열 처리 이벤트 발행 및 소비
* 처리 과정:
    1. Redis에서 상위 N명의 사용자 조회
    2. 각 사용자에 대해 접근 토큰 생성 및 저장
    3. 대기열에서 해당 사용자 제거
    4. Kafka를 통해 접근 권한 부여 이벤트 발송

5. 동시 접속자 및 대기 시간 계산

* 예상 좌석 선택 및 예약 완료 시간: 3분
* 시스템 처리 용량: 분당 2,000명 (약 33 TPS)
* 좌석 조회 및 예약 API 호출 횟수: 3회 (좌석 조회, 좌석 선택, 결제)
* 분당 처리 가능한 실제 사용자 수: 약 660명
* 대기열 처리: 10초마다 110명씩 접근 토큰 발급
* 대기 순서 및 예상 대기 시간 계산:
    - 대기 순서 = 현재 사용자의 대기열 위치
    - 예상 대기 시간(분) = (대기 순서 / 110) * (10 / 60)

6. 구현 상세

* QueueService:
    - enterQueue(): 사용자를 대기열에 추가
    - getQueueStatus(): 현재 대기 상태 조회
    - processQueue(): 대기열에서 사용자를 제거하고 접근 토큰 발급
* ReservationService:
    - getTokenStatus(): 토큰 상태 및 유효성 확인
* Redis 작업:
    - 대기열 추가/제거: ZADD, ZREM 명령어 사용
    - 대기 순서 조회: ZRANK 명령어 사용
    - 토큰 저장 및 조회: SET, GET 명령어 사용 (만료 시간 설정)
* 동시성 처리:
    - Redis의 원자적 작업을 활용하여 동시성 이슈 최소화
    - 낙관적 락을 사용하여 동시 수정 충돌 방지

---

## 5. Transaction 범위와 책임 분리 방안 설계

[Transaction 범위와 책임 분리 방안 설계](https://iwannabarmus.tistory.com/38)

---

## 6. 부하테스트 & 장애 대응

[k6로 부하테스트 해보기](https://iwannabarmus.tistory.com/41)


---

## Trouble Shooting

*모든 과정을 기록할 수는 없었지만, 5주차 진행 과정에서의 나의 트러블 슈팅 과정을 기록하였다.*

[Trouble Shooting 기록 바로가기](https://iwannabarmus.tistory.com/27)





---

## 주차별 인사이트

<details>
<summary>1주차 인사이트</summary>
<div markdown="1">

#### 1. **모놀리식 프로젝트의 개발 환경 구성**

- **구성 요소**: Docker, Gradle, Spring Boot, MySQL을 사용하여 개발 환경을 구성.
- **프로젝트 구조**:
    - Dockerfile과 Gradle 설정 파일
    - MySQL 데이터베이스 초기화 스크립트
    - Spring Boot 애플리케이션 소스 코드
- **설정 파일**:
    - `docker-compose.yml`을 통해 데이터베이스와 애플리케이션 서비스 정의
    - `build.gradle`에서 의존성 관리 및 빌드 설정

#### 2. **도메인 주도 설계(DDD)로 전환**

- **문제점**: 모놀리식 아키텍처의 확장성 한계, 유지보수 어려움, 협업 비효율성
- **해결책**:
    - DDD 패턴 도입으로 도메인 지식 반영, 책임 분리, 유연한 확장성 확보
    - 효율적인 협업, 유지보수성 향상, 비즈니스 로직 반영

#### 3. **환경 변수 설정 및 트러블슈팅**

- **추가된 설정**:
    - MySQL의 `hikari` 설정 (풀 사이즈, 타임아웃 등)
    - 에러 페이지, 서블릿 파라미터 등 서버 설정
- **트러블슈팅 사례**:
    - Docker Compose 파일 위치 문제: `application.yml`에서 Docker Compose 자동 실행 비활성화

#### 4. **레포지토리 추상화**

- **장점**:
    - 구현체 교체의 용이성 (JPA, NoSQL 등)
    - 비즈니스 로직과 데이터 접근 로직의 분리
    - 테스트 용이성 (Mock 객체 활용)
    - 유연성과 확장성
    - 디자인 패턴의 장점 (Repository Pattern)

#### 5. **로그인/로그아웃 구현**

- **로그인**:
    - `AuthenticationManager`를 통한 인증 후 `SecurityContextHolder`에 사용자 정보 저장
    - JWT 토큰 생성 및 반환
- **로그아웃**:
    - `accessToken` 유효성 검증 후 사용자 정보 삭제
    - `refresh_tokens` 테이블에서 리프레시 토큰 삭제

#### 6. **데이터베이스 선택 및 관리**

- **요구사항**:
    - 데이터 무결성, 복잡한 데이터 관계 관리, 강력한 쿼리 기능, 확장성과 고가용성, 보안 및 접근 제어
- **MySQL 선택 이유**:
    - 사용의 용이성, 광범위한 호환성, 강력한 성능과 안정성, 비용 효율성

#### 7. **ERD 설계 중요 요소**

- **중요한 점**:
    - 데이터 간의 관계와 무결성 유지
    - 정규화를 통한 중복 최소화
    - 각 엔티티의 속성과 제약 조건 정의

### 결론

1주차 데이터에서는 프로젝트의 기본적인 개발 환경 설정, 아키텍처 전환, 환경 변수 설정, 데이터 접근의 추상화, 로그인/로그아웃 구현, 데이터베이스 선택 및 관리, 그리고 ERD 설계의 중요한 요소들을
다루었습니다. 이를 통해 데이터 무결성, 확장성, 유지보수성, 보안 등의 중요한 요구사항을 충족할 수 있는 방법들을 적용하였습니다.

</div>
</details>

<details>
<summary>2주차 인사이트</summary>
<div markdown="2">

#### 1. **동시성 관리**

- **고민 사항**: 여러 사용자가 동시에 대기열에 진입하거나 대기 상태를 확인할 때 발생하는 데이터 꼬임과 시스템 성능 저하.
- **해결 방안**: 데이터베이스의 동시성 제어 기능을 활용. 락(lock) 메커니즘이나 트랜잭션을 통해 데이터를 안전하게 관리.

#### 2. **성능 최적화**

- **고민 사항**: 대기열에 많은 사용자가 진입할 때 발생하는 성능 저하.
- **해결 방안**: 효율적인 쿼리 작성과 인덱싱을 통해 데이터베이스 접근을 최적화. 캐싱 도입으로 빈번한 데이터베이스 조회를 줄임.

#### 3. **자동 만료 처리**

- **고민 사항**: 일정 시간 동안 대기 상태가 유지될 때 이를 자동으로 만료시키는 문제.
- **해결 방안**: 스케줄러를 도입하여 주기적으로 대기열 상태를 확인하고, 만료된 항목을 처리. 데이터베이스에서 제거하거나 상태를 변경.

#### 4. **순번 관리**

- **고민 사항**: 대기 상태인 사용자 중에서 누가 먼저 활성 상태로 전환될지 관리하는 문제.
- **해결 방안**: FIFO 방식이나 우선순위 큐를 사용해 순번을 정확히 관리. 순번 관리 로직을 잘 설계.

#### 5. **토큰 발급 및 검증**

- **고민 사항**: 사용자가 대기열에 진입할 때 유일한 토큰을 발급하고 이를 통해 사용자 식별.
- **해결 방안**: JWT를 사용해 토큰을 발급하고 검증. 보안성을 높이고 변조를 방지.

#### 6. **사용자 경험**

- **고민 사항**: 대기열에 있는 사용자가 자신의 상태를 쉽게 확인하고 예상 대기 시간을 알 수 있도록 하는 문제.
- **해결 방안**: 사용자에게 대기 상태와 예상 대기 시간을 명확히 전달하는 API와 UI 제공. WebSocket이나 SSE를 사용해 실시간으로 상태를 업데이트.

#### 7. **데이터 일관성 및 복구**

- **고민 사항**: 시스템 오류나 예기치 않은 상황에서 데이터 일관성을 유지하고 복구하는 문제.
- **해결 방안**: 데이터베이스의 트랜잭션 관리와 롤백 메커니즘 활용. 로그와 백업을 통해 복구 가능한 시스템 설계.

#### 8. **확장성**

- **고민 사항**: 시스템이 확장 가능하도록 설계.
- **해결 방안**: 마이크로서비스 아키텍처 도입. 클라우드 인프라 활용해 자원을 동적으로 할당.

### 리팩토링 및 MSA 전환

1. **엔티티 분리**

- 기존 모놀리식 구조에서 각 서비스(User, Reservation, Concert)의 엔티티를 분리. 각 서비스는 자체 엔티티를 관리하고, 다른 서비스의 엔티티와의 직접 참조를 제거.

2. **서비스 간 통신**

- Kafka를 사용해 서비스 간 통신. 각 서비스가 필요한 이벤트를 발행하고, 다른 서비스에서 이를 구독하여 처리.

3. **API 게이트웨이**

- Spring Cloud Gateway를 사용해 API 게이트웨이를 구현. JWT 토큰을 파싱하여 사용자 정보를 각 서비스에 전달.

4. **데이터 일관성 유지**

- 초기에는 결과적 일관성(Eventual Consistency)을 목표로 하고, 필요에 따라 점진적으로 개선. Kafka를 통한 이벤트 기반 아키텍처 활용.

5. **점진적 리팩토링**

- 서비스 분리: 각 도메인별로 서비스를 분리.
- 데이터베이스 분리: 각 서비스의 데이터베이스를 분리.
- 서비스 간 통신 구현: Kafka를 통한 통신 구현.
- API 게이트웨이 구현: 인증/인가 및 라우팅 담당.
- 점진적 개선: 성능, 확장성 모니터링 및 개선.

### 결론

이번 2주차 인사이트에서는 대기열 기능 구현 시의 고민 사항과 해결 방안을 중심으로, 서비스 리팩토링 및 MSA 전환에 필요한 개념과 전략을 학습하고 일부 시행했습니다.
이를 통해 시스템의 안정성과 확장성을 높이는 데 기여했습니다.

</div>
</details>

<details>
<summary>3주차 인사이트</summary>
<div markdown="3">

### 1. 프로젝트 구조 및 변경 사항

#### 마이크로서비스 아키텍처 전환

- **모놀리식에서 MSA로 전환**: 프로젝트가 단일 모놀리식 데이터베이스에서 서비스별 데이터베이스를 가지는 마이크로서비스 아키텍처로 전환되었습니다.
- **서비스별 데이터베이스**: User Service, Reservation Service, Concert Service 각각 독립적인 MySQL 컨테이너로 구성되었습니다.
- **Docker Compose**: 모든 데이터베이스 서비스를 하나의 `compose.yml` 파일로 관리하여 일괄적인 실행과 관리가 가능하게 되었습니다.

#### Core 모듈의 리팩토링

- **공통 기능 유지**: 공통으로 사용되는 클래스는 core 모듈에 유지되었습니다.
- **서비스 특화 기능 이동**: 특정 서비스에 종속적인 클래스는 해당 서비스 모듈로 이동되었습니다.
- **인터페이스 추상화**: 공통적으로 사용되는 서비스는 인터페이스만 core 모듈에 두고 구현은 각 서비스에서 하도록 변경되었습니다.
- **공통 유틸리티 집중**: 공통적으로 사용되는 유틸리티 클래스는 core 모듈에 집중시켜 중복을 방지했습니다.

### 2. 주요 구현 사항

#### API Gateway 서비스 구현

- **Spring Cloud Gateway** 기반의 API Gateway 서비스 구현.
- **주요 기능**: 요청 라우팅, JWT 인증 및 인가, 요청/응답 필터링, 로드 밸런싱, 서비스 디스커버리 통합.
- **JWT 인증**: AuthorizationHeaderFilter를 통해 JWT 토큰 검증을 수행하고 유효한 토큰에서 사용자 정보를 추출하여 헤더에 추가.

#### build.gradle 설정 개선

- **플러그인 적용**: 필요한 플러그인은 서브프로젝트에서만 적용하도록 설정.
- **공통 설정**: 모든 프로젝트와 서브프로젝트에 적용될 설정을 `allprojects`와 `subprojects` 블록에서 정의.
- **의존성 관리**: 공통 의존성은 루트 `build.gradle` 파일에서 관리하고, 각 모듈에서는 필요한 추가 의존성을 정의.
- **QueryDSL 설정**: 필요한 서브프로젝트에 QueryDSL 설정을 적용하도록 클로저를 정의.

### 3. 문제 해결

#### Core 모듈 의존성 문제 해결

- **불필요한 클래스 제거**: 특정 서비스에 종속적인 클래스는 core 모듈에서 제거하고 해당 서비스로 이동.
- **공통 유틸리티 유지**: 여러 서비스에서 공통으로 사용하는 유틸리티 클래스는 core 모듈에 남김.
- **인터페이스 분리**: 공통 인터페이스는 core에 유지하고, 구현은 각 서비스 모듈에서 하도록 변경.

#### 빌드 오류 해결

- **Gradle 설정 파일 수정**: `settings.gradle`과 `build.gradle` 파일을 수정하여 모든 모듈이 올바르게 인식되도록 함.
- **의존성 명확화**: 각 모듈의 `build.gradle` 파일에서 필요한 의존성을 명확하게 선언.
- **플러그인 적용 문제 해결**: 루트 `build.gradle` 파일에서 플러그인을 올바르게 적용하고, 각 서브프로젝트에서 필요한 플러그인을 적용하도록 수정.

</div>
</details>

<details>
<summary>4주차 인사이트</summary>
<div markdown="4">

### 1. 트랜잭션과 락 메커니즘

#### **주요 내용:**

- 트랜잭션의 ACID 속성과 데이터 일관성을 보장하기 위한 비관적 락과 낙관적 락의 사용.
- 트랜잭션 격리 수준 설정과 락 메커니즘을 통해 동시성 문제를 해결.

#### **인사이트:**

- 비관적 락과 낙관적 락은 각각의 특성과 장단점이 있으므로, 시스템의 동시성 요구사항에 따라 적절히 선택하여 사용해야 합니다.
- 트랜잭션 격리 수준을 적절히 설정하여 데이터 일관성과 성능을 최적화할 수 있습니다.
- 락 메커니즘을 효율적으로 사용하여 동시성 문제를 해결하고, 데이터베이스 성능을 향상시킬 수 있습니다.

### 2. 필터 및 예외 처리

#### **주요 내용:**

- Spring Cloud Gateway를 사용한 요청 본문 처리 및 필터 설정.
- AuthorizationHeaderFilter와 RequestBodyFilter의 설정 및 문제 해결.
- 예외 발생 시 로깅 및 재처리를 통한 시스템 안정성 확보.

#### **인사이트:**

- 필터 체인을 통해 요청 본문과 헤더를 적절히 처리하는 것이 중요합니다. 이를 통해 시스템의 일관성을 유지할 수 있습니다.
- 예외 처리 로직을 강화하여 시스템의 신뢰성을 높이고, 문제 발생 시 원인을 쉽게 추적할 수 있도록 합니다.

### 3. 성능 테스트 및 최적화

#### **주요 내용:**

- 다양한 부하 테스트 도구(JMeter, Locust, K6, Python's requests)를 사용하여 성능 테스트를 실시.
- 테스트 결과를 바탕으로 성능 병목 지점을 식별하고 최적화 방안을 모색.

#### **인사이트:**

- 성능 테스트 도구의 선택은 시스템의 특성과 요구사항에 따라 달라져야 합니다. 예를 들어, JMeter는 GUI를 통한 사용이 쉬운 반면, Locust와 K6는 코드 기반 설정으로 유연성이 높습니다.
- 성능 테스트 결과를 분석하여 병목 지점을 식별하고, 이를 개선하기 위한 구체적인 방안을 마련하는 것이 중요합니다.
- 응답 시간, 처리량, 오류율 등을 종합적으로 분석하여 시스템의 성능을 최적화할 수 있습니다.

### 4. 이벤트 기반 아키텍처

#### **주요 내용:**

- 서비스 간의 독립성을 유지하고, 확장성과 유연성을 높이기 위한 이벤트 기반 아키텍처 설계.
- Kafka를 사용한 서비스 간 메시지 브로커 역할과 이벤트 드리븐 아키텍처 구현.

#### **인사이트:**

- 이벤트 기반 아키텍처를 통해 서비스 간의 느슨한 결합을 유지하고, 확장성과 유연성을 높일 수 있습니다.
- Kafka를 사용하여 서비스 간의 비동기 통신을 구현함으로써 시스템의 성능을 향상시키고, 처리량을 증가시킬 수 있습니다.
- 이벤트 드리븐 아키텍처는 시스템의 확장성과 유연성을 높이기 위한 중요한 요소입니다.

### 5. 데이터베이스 설계 및 인덱스 최적화

#### **주요 내용:**

- 각 서비스에 독립적인 데이터베이스를 사용하여 데이터 일관성 확보.
- 인덱스 설정을 통한 데이터베이스 성능 최적화.
- 효율적인 쿼리 작성과 데이터베이스 락 메커니즘 적용.

#### **인사이트:**

- 데이터베이스 설계 시 각 서비스의 요구사항을 반영하여 테이블 구조를 최적화하는 것이 중요합니다.
- 인덱스를 적절히 설정함으로써 검색 속도를 향상시키고, 쿼리 성능을 최적화할 수 있습니다.
- 트랜잭션과 락 메커니즘을 효과적으로 사용하여 데이터 일관성과 무결성을 유지할 수 있습니다.

### 6. 인프라 구성 및 최적화

#### **주요 내용:**

- Spring Cloud API Gateway와 Eureka Discovery Server를 사용한 마이크로서비스 인프라 구성.
- Redis를 이용한 캐싱과 데이터 저장.
- 인프라 모니터링 및 성능 최적화 방안.

#### **인사이트:**

- Spring Cloud API Gateway와 Eureka를 사용하여 마이크로서비스 간의 서비스 디스커버리와 라우팅을 효율적으로 관리할 수 있습니다.
- Redis를 활용한 캐싱은 데이터 접근 속도를 향상시키고, 데이터베이스의 부하를 줄일 수 있습니다.
- 인프라 모니터링 도구를 사용하여 시스템의 성능을 지속적으로 모니터링하고, 병목 지점을 식별하여 최적화하는 것이 중요합니다.

### 7. API 설계 및 문서화

#### **주요 내용:**

- RESTful API 설계 및 명세서 작성.
- 엔드포인트 정의와 요청/응답 데이터 구조 설계.
- API 문서화를 통해 개발자 간의 의사소통 원활화.

#### **인사이트:**

- 명확하고 일관된 API 설계를 통해 시스템의 유지보수성을 높일 수 있습니다.
- API 문서화를 통해 개발자 간의 의사소통을 원활하게 하고, 개발 속도를 향상시킬 수 있습니다.
- RESTful API 설계 원칙을 준수하여, 확장성과 유연성을 갖춘 API를 제공할 수 있습니다.

### 종합 결론

4주차 동안의 데이터는 MSA 구조에서의 예약 시스템 구현 및 최적화에 중점을 두고 있습니다. 이를 통해 다음과 같은 주요 인사이트를 도출할 수 있습니다:

- 트랜잭션과 락 메커니즘을 효율적으로 사용하여 동시성 문제를 해결하고, 데이터 일관성과 성능을 향상시킬 수 있습니다.
- 필터 체인을 통해 요청 본문과 헤더를 적절히 처리하고, 예외 처리 로직을 강화하여 시스템의 안정성과 신뢰성을 높이는 것이 필요합니다.
- 성능 테스트 도구의 선택은 시스템의 특성과 요구사항에 따라 달라져야 하며, 성능 테스트 결과를 분석하여 병목 지점을 식별하고 최적화하는 것이 중요합니다.
- 이벤트 기반 아키텍처를 통해 서비스 간의 독립성을 유지하고, 시스템의 확장성과 유연성을 높일 수 있습니다.
- 데이터베이스 성능 최적화를 위해 인덱싱과 단일 `UPDATE` 쿼리를 사용하는 것이 효과적입니다.
- Spring Cloud API Gateway와 Eureka를 사용하여 마이크로서비스 간의 서비스 디스커버리와 라우팅을 효율적으로 관리할 수 있습니다.
- 명확하고 일관된 API 설계를 통해 시스템의 유지보수성을 높일 수 있습니다.

</div>
</details>

---

## 기술 스택

1. 주요 프레임워크 및 라이브러리:
    - Spring Boot 3.3.0
    - Spring Data JPA 3.3.0
    - Spring Web 3.3.0
    - Spring Security 3.3.0
    - Spring Validation 3.3.0
    - Spring Cloud Netflix Eureka Client 4.1.2
    - Spring Cloud OpenFeign 4.1.2
    - Spring Data Redis 3.3.0
    - Spring Kafka 3.2.0
    - Spring Boot Actuator 3.3.0
    - Spring Boot Mail 3.3.0
    - Spring Cloud Gateway 4.1.4


2. 데이터베이스:
    - MySQL Connector/J 8.3.0
    - Redisson 3.24.3


3. 쿼리 및 ORM:
    - Querydsl JPA 5.0.0


4. 메시징:
    - Apache Kafka (Spring Kafka 3.2.0에 의해 관리)


5. 인증 및 보안:
    - JSON Web Token (JWT) 0.12.5 (jwt-api, jwt-impl, jwt-jackson)


6. 로깅:
    - Log4j2 2.23.1 (log4j-api, log4j-core, log4j-slf4j2-impl)


7. 개발 도구:
    - Lombok 1.18.32
    - Netty Resolver DNS Native MacOS 4.1.68.Final


8. 빌드 및 의존성 관리:
    - Gradle 8.8
    - Spring Dependency Management Plugin 1.1.5


9. Java 버전:
    - Java 17

## 테스트

- 테스트 코드 주석 참고

