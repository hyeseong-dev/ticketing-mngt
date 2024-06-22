- 사용자 (`users`)는 여러 리프레시 토큰 (`refresh_tokens`)을 가질 수 있습니다. 
  - 1(users): N(refresh_tokens) 관계
   

- 사용자 (`users`)는 여러 예약 (`reservations`)과 구매 (`purchases`)를 할 수 있습니다.
    - 1(users): N(reservations) 관계
    - 1(users): M(purchases) 관계


- 사용자 (`users`)는 여러 트랜잭션 (`transactions`)을 가질 수 있습니다.
    - 1(users): N(transactions) 관계


- 공연 (`events`)은 여러 공연 날짜 (`event_dates`)를 가질 수 있습니다.
    - 1(events): N(event_dates) 관계


- 공연 날짜 (`event_dates`)는 여러 좌석 (`seats`)을 가질 수 있습니다.
    - 1(event_dates): N(seats) 관계


- 좌석 (`seats`)은 여러 예약 (`reservations`)과 구매 (`purchases`)를 가질 수 있습니다.
    - 1(seats): N(reservations) 관계
    - 1(seats): M(purchases) 관계
    - 
    
- 공연 날짜 (`event_dates`)는 여러 트랜잭션 (`transactions`)을 가질 수 있습니다.
    - 1(event_dates): N(transactions) 관계
   
 