# 방탈출 예약 관리

## Spring MVC 인증 (1~3단계)

### 미션 소개

JWT와 Cookie를 이용한 로그인 기능을 구현하고, Spring MVC의 `HandlerMethodArgumentResolver`와 `HandlerInterceptor`를 활용하여 로그인 사용자 정보 주입과 관리자 권한 검사를 처리

### 요구사항

- 이메일과 비밀번호를 이용하여 로그인할 수 있다.
- 로그인 성공 시 JWT 토큰을 Cookie에 저장한다.
- Cookie의 인증 정보를 이용하여 로그인 사용자 정보를 조회할 수 있다.
- `HandlerMethodArgumentResolver`를 활용하여 로그인 사용자 정보를 Controller에 주입한다.
- 예약 생성 시 요청에 `name`이 없으면 로그인한 사용자 정보로 예약을 생성한다.
- `HandlerInterceptor`를 활용하여 관리자 페이지 접근 권한을 검사한다.
- 관리자 전용 테마/시간 생성·삭제 API에도 권한 검사를 적용한다.

### 주요 구현 사항

#### 로그인 및 인증 정보 조회

로그인 성공 시 회원 정보를 기반으로 JWT 토큰을 생성하고 `token` Cookie로 응답합니다.

```text
email, password
→ Member 조회
→ JWT 생성
→ Cookie 저장
```

인증 정보 조회 시에는 Cookie의 token에서 회원 ID를 추출하여 로그인 사용자를 조회합니다.

#### HandlerMethodArgumentResolver

Cookie를 이용해 로그인 회원을 조회하는 로직을 `LoginMemberArgumentResolver`로 분리하였습니다.

```text
Cookie
→ token 추출
→ memberId 추출
→ Member 조회
→ LoginMember 생성
→ Controller에 주입
```

이를 통해 Controller에서 Cookie와 JWT 처리 로직을 직접 다루지 않도록 하였습니다.

#### 예약 생성 리팩터링

예약 생성 요청의 `name` 존재 여부에 따라 예약자를 결정합니다.

```text
name 있음 → name으로 Member 조회
name 없음 → LoginMember로 Member 조회
```

또한 웹 요청 객체인 `ReservationRequest`가 DAO까지 전달되지 않도록 `ReservationSaveCommand`를 추가하여 저장에 필요한 데이터만 전달하도록 변경하였습니다.

#### 관리자 권한 검사

`AdminInterceptor`를 이용하여 관리자 페이지 및 관리자 전용 API의 접근 권한을 검사합니다.

테마와 시간의 생성/삭제 API에는 `@AdminOnly` 커스텀 어노테이션을 적용하여 관리자 권한이 필요한 메서드를 구분하였습니다.

```java
@AdminOnly
@PostMapping("/themes")
```

```java
@AdminOnly
@DeleteMapping("/times/{id}")
```

관리자 권한이 없는 경우 미션 요구사항에 따라 `401 Unauthorized`를 응답합니다.

### 추가 리팩터링

- 회원 역할을 `String` 대신 `Role` Enum으로 관리
- Cookie의 token 추출 로직을 `TokenCookieExtractor`로 분리
- 회원 조회 실패 시 `MemberNotFoundException`으로 변환
- `@Valid`를 이용하여 예약 생성 요청 검증
- `@RestControllerAdvice`를 이용하여 검증 실패 예외를 공통 처리
- `member.name`에 `UNIQUE` 제약 추가

### 주요 객체

| 객체 | 역할 |
| --- | --- |
| `AuthController` | 로그인 및 로그인 사용자 정보 조회 API를 처리한다. |
| `TokenProvider` | JWT 토큰을 생성하고 회원 ID를 추출한다. |
| `TokenCookieExtractor` | Cookie에서 token을 추출한다. |
| `LoginMemberArgumentResolver` | 로그인 사용자 정보를 생성하여 Controller에 주입한다. |
| `AdminInterceptor` | 관리자 권한이 필요한 요청을 검사한다. |
| `AdminOnly` | 관리자 전용 Controller 메서드를 표시한다. |
| `Role` | 회원 역할인 `ADMIN`, `USER`를 관리한다. |
| `ReservationSaveCommand` | 예약 저장에 필요한 데이터를 DAO에 전달한다. |
| `GlobalExceptionHandler` | 요청 검증 실패 등의 예외를 HTTP 응답으로 변환한다. |

### 테스트

#### 1단계 - 로그인

- 로그인 성공 시 JWT token Cookie 생성 확인
- Cookie를 이용한 로그인 사용자 정보 조회 확인

#### 2단계 - 로그인 리팩터링

- 로그인 사용자 정보를 이용한 예약 생성 확인
- `name`이 존재할 경우 해당 회원으로 예약 생성 확인

#### 3단계 - 관리자 기능

- 일반 회원의 `/admin` 접근 시 `401 Unauthorized` 확인
- 관리자 회원의 `/admin` 접근 시 `200 OK` 확인

--

## Spring Data JPA (4~6단계)

### 미션 소개

기존 JDBC 기반 데이터 접근 방식을 Spring Data JPA로 전환하고, 로그인 사용자의 예약 조회와 예약 대기 기능을 구현

### 주요 구현 사항

#### JPA 기반 데이터 접근

기존 `JdbcTemplate` 기반 DAO를 `JpaRepository` 기반 Repository로 전환하였습니다.

`Reservation`, `Member`, `Theme`, `Time` 간 관계는 JPA 연관관계로 매핑하고, 필요한 조회는 Query Method와 JPQL을 활용하였습니다.

#### 내 예약 목록 조회

로그인한 사용자의 `memberId`를 기준으로 예약 목록을 조회하도록 구현하였습니다.

일반 사용자 예약은 `member_id`를 저장하고, 관리자 화면에서 생성한 예약은 기존처럼 `name` 값을 저장하도록 구성하였습니다.

```text
일반 사용자 예약 → member_id 저장
관리자 예약 → name 저장
```

#### 예약 대기 신청

예약된 날짜, 테마, 시간에 대해 예약 대기를 신청할 수 있도록 구현하였습니다.

```http
POST /waitings
Content-Type: application/json
Cookie: token=...
```

```json
{
  "date": "2024-03-01",
  "theme": 1,
  "time": 1
}
```

응답에는 생성된 예약 대기 ID와 현재 대기 순서를 포함합니다.

```json
{
  "id": 1,
  "waitingNumber": 1
}
```

#### 예약 대기 취소

본인이 신청한 예약 대기만 취소할 수 있도록 구현하였습니다.

```http
DELETE /waitings/{id}
```

성공 시 `204 No Content`를 응답합니다.

#### 예약 대기 비즈니스 규칙

예약 대기 생성 시 다음 조건을 검증합니다.

- 해당 날짜, 테마, 시간에 기존 예약이 존재해야 한다.
- 이미 해당 예약을 가지고 있는 사용자는 대기할 수 없다.
- 동일한 예약에 중복으로 대기할 수 없다.
- 다른 사용자의 예약 대기는 취소할 수 없다.

#### 예약 대기 순번 조회

같은 날짜, 테마, 시간의 예약 대기 중 현재 대기보다 먼저 생성된 개수를 JPQL로 조회합니다.

```text
먼저 생성된 대기 수 + 1
→ 현재 대기 순번
```

이를 이용하여 내 예약 목록에서 다음과 같이 상태를 표시합니다.

```text
예약
1번째 예약대기
2번째 예약대기
```

### 내 예약 목록 통합 조회

`GET /reservations-mine` 요청 시 확정된 예약뿐만 아니라 예약 대기 목록도 함께 조회하여 반환합니다.

예약과 예약 대기는 `MyReservationResponse`로 통합하여 응답하고, `status`를 통해 상태를 구분합니다.

### 주요 객체

| 객체 | 역할 |
| --- | --- |
| `Reservation` | 확정된 예약 정보를 관리 |
| `Waiting` | 예약 대기 정보를 관리 |
| `WaitingWithRank` | 예약 대기 정보와 대기 순번을 함께 표현 |
| `WaitingRequest` | 예약 대기 생성 요청 DTO |
| `WaitingResponse` | 예약 대기 생성 결과 응답 DTO |
| `MyReservationResponse` | 내 예약 및 예약 대기 목록 응답 DTO |
| `ReservationRepository` | 예약 데이터 접근 |
| `WaitingRepository` | 예약 대기 데이터 접근 및 순번 조회 |

### 테스트

#### 4단계 - JPA 전환

- `TestEntityManager`를 이용한 `Time` 엔티티 저장 확인
- `TimeRepository`를 이용한 엔티티 조회 확인

#### 5단계 - 내 예약 목록 조회

- 관리자 로그인 후 `/reservations-mine` 요청 시 `200 OK` 응답 확인
- 로그인 사용자의 예약 목록이 정상적으로 조회되는지 확인
- 조회된 예약 목록의 개수 확인

#### 6단계 - 예약 대기 기능

- 예약 대기 생성 시 `201 Created` 응답 확인
- 예약 대기 생성 후 `/reservations-mine` 조회 시 대기 항목 포함 여부 확인
- 예약 대기 상태가 `1번째 예약대기` 형식으로 반환되는지 확인
