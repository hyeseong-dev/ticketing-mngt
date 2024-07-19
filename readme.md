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
- [개발하면서 끄적](##5.-개발하면서-끄적)
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

