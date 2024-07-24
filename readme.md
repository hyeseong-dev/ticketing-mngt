여기 문서의 목차를 마크다운 문법에 맞게 수정한 버전입니다:

# 콘서트 티켓 좌석 예매

## 목차

1. [프로젝트 Milestone](###1-프로젝트-milestone)
2. [POSTMAN API Docs](###2-postman-api-docs)
3. [시스템 아키텍처](###3-시스템-아키텍처)
4. [UML 다이어그램](###4-uml-다이어그램)
5. [대기열 설계 및 구현](##5-대기열-설계-및-구현)
6. [인사이트](###6-인사이트)
7. [기술 스택](###7-기술-스택)

---

### 1.프로젝트 Milestone

[구글닥스 문서 바로가기](https://docs.google.com/spreadsheets/d/17yUn-cEa9uq2jE7_bTpjfXoeaXIAGt_91FmHhMm2BJo/edit?gid=0#gid=0)

### 2.POSTMAN API Docs

[POSTMAN UI](https://documenter.getpostman.com/view/14042841/2sA3kSoPGR)

### 3.시스템 아키텍처

![시스템아키텍처](./docs/MSA-EDA-구조.drawio.svg)

### 4.UML 다이어그램

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

### 5.대기열 설계 및 구현

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

### 6.인사이트

<details>
<summary>내용</summary>
<div markdown="1">

1. 마이크로서비스 아키텍처(MSA) 전환
   모놀리식 구조에서 MSA로의 전환은 시스템의 확장성과 유연성을 크게 향상시켰습니다. 각 서비스(User, Reservation, Concert)가 독립적인 데이터베이스를 가짐으로써 서비스 간 결합도를 낮추고
   개별 서비스의 독립적인 개발과 배포가 가능해졌습니다.

- 이는 시스템 전체의 유지보수성을 높이고, 각 서비스의 성능 최적화를 용이하게 만들었습니다. 또한, 서비스별 스케일링이 가능해져 리소스 활용의 효율성이 증가했습니다.

2. 동시성 제어 및 데이터 일관성 관리
   트랜잭션과 락 메커니즘을 효과적으로 활용하여 동시성 문제를 해결하고 데이터 일관성을 보장했습니다. 비관적 락과 낙관적 락을 시스템 요구사항에 맞게 적용하여 성능과 데이터 정확성 사이의 균형을 맞추었습니다.

- 특히, 예약 시스템에서 중요한 동시 접근 문제를 해결하기 위해 트랜잭션 격리 수준을 적절히 설정하고, 데이터베이스 락을 효과적으로 사용했습니다. 이를 통해 시스템의 신뢰성과 안정성을 크게 향상시켰습니다.

3. API Gateway 구현
   Spring Cloud Gateway를 기반으로 한 API Gateway 서비스 구현은 마이크로서비스 아키텍처의 핵심 요소로 작용했습니다. 이를 통해 요청 라우팅, JWT 인증 및 인가, 요청/응답 필터링,
   로드 밸런싱, 서비스 디스커버리 등의 중앙화된 관리가 가능해졌습니다.

- API Gateway는 클라이언트와 마이크로서비스 사이의 단일 진입점 역할을 함으로써 보안을 강화하고, 클라이언트 요청을 효율적으로 처리할 수 있게 했습니다. 이는 전체 시스템의 성능과 관리 효율성을 크게
  향상시켰습니다.

4. 이벤트 기반 아키텍처 도입
   Kafka를 활용한 이벤트 기반 아키텍처의 도입은 서비스 간 느슨한 결합을 가능하게 하여 시스템의 확장성과 유연성을 크게 향상시켰습니다. 이를 통해 비동기 통신이 가능해져 시스템의 전반적인 성능과 처리량이
   개선되었습니다.

- 또한, 이벤트 드리븐 방식은 복잡한 비즈니스 프로세스를 더 효과적으로 모델링하고 관리할 수 있게 해주었습니다. 이는 시스템의 유지보수성을 높이고, 새로운 기능 추가를 용이하게 만들었습니다.

5. 성능 테스트 및 최적화
   다양한 부하 테스트 도구(JMeter, Locust, K6, Python's requests)를 사용하여 체계적인 성능 테스트를 수행했습니다. 이를 통해 시스템의 병목 지점을 정확히 식별하고, 구체적인 최적화
   방안을 도출할 수 있었습니다.

- 성능 테스트 결과를 바탕으로 응답 시간, 처리량, 오류율 등을 종합적으로 분석하여 시스템 성능을 최적화했습니다. 이는 실제 운영 환경에서의 안정성과 사용자 경험 향상으로 이어졌습니다.

6. 데이터베이스 최적화
   각 서비스에 독립적인 데이터베이스를 사용함으로써 데이터 관리의 효율성을 높였습니다. 또한, 인덱스 설정 최적화와 효율적인 쿼리 작성을 통해 데이터베이스 성능을 크게 개선했습니다.

- 특히, 예약 시스템에서 중요한 동시성 제어를 위해 데이터베이스 락 메커니즘을 효과적으로 적용했습니다. 이를 통해 데이터 일관성을 유지하면서도 높은 처리량을 달성할 수 있었습니다.

7. 캐싱 전략 구현
   Redis를 이용한 캐싱 전략 구현은 데이터 접근 속도를 크게 향상시키고 데이터베이스의 부하를 효과적으로 줄였습니다. 이는 특히 자주 접근되는 데이터에 대한 빠른 응답 시간을 보장하여 전체적인 시스템 성능을
   개선했습니다.

- 또한, 캐싱을 통해 네트워크 트래픽과 데이터베이스 연산을 줄임으로써 시스템의 확장성과 비용 효율성을 높였습니다. 이는 대규모 사용자를 처리해야 하는 예약 시스템에서 특히 중요한 역할을 했습니다.

8. 서비스 디스커버리 구현
   Eureka Discovery Server를 활용한 서비스 디스커버리 구현은 동적으로 변화하는 마이크로서비스 환경에서 효과적인 서비스 관리를 가능하게 했습니다. 이를 통해 서비스 인스턴스의 자동 등록과 해제,
   그리고 클라이언트 사이드 로드 밸런싱이 가능해졌습니다.

- 서비스 디스커버리는 시스템의 유연성과 확장성을 크게 향상시켰으며, 서비스 간 통신의 신뢰성을 높였습니다. 이는 마이크로서비스 아키텍처의 핵심 이점을 최대한 활용할 수 있게 해주었습니다.

9. 대기열 관리 시스템 구현
   대규모 동시 접속 환경에서의 효율적인 예약 처리를 위해 대기열 관리 시스템을 구현했습니다. 이를 통해 시스템 과부하를 방지하고 공정한 예약 기회를 제공할 수 있었습니다. Redis를 활용한 분산 락 메커니즘을
   통해 대기열의 동시성 문제를 해결했습니다.

- 대기열 시스템은 사용자 경험을 개선하고 시스템의 안정성을 높이는 데 크게 기여했습니다. 실시간 대기 상태 업데이트와 예상 대기 시간 제공 등의 기능을 통해 사용자 만족도를 높일 수 있었습니다.

</div>
</details>

---

### 7. 기술 스택

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

 
