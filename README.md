# spring-basic-roomescape-playground

Spring MVC 인증 및 JPA 전환 미션의 1~6단계를 구현한 방탈출 예약 관리 프로젝트입니다.
JWT와 쿠키를 이용한 로그인, 로그인 회원 정보 주입, 관리자 페이지·API 접근 제한과 JPA 기반 데이터 저장·조회를 제공합니다.


## 클래스별 구현 기능 목록

---

## `WebMvcConfig`

### 로그인 회원 정보 주입 설정

- [x] `WebMvcConfigurer`를 구현해 Spring MVC 설정을 확장한다.
- [x] `addArgumentResolvers()`에 `LoginMemberArgumentResolver`를 등록한다.
- [x] 컨트롤러의 `LoginMember` 매개변수에 로그인 회원 정보를 전달할 수 있도록 설정한다.

### 관리자 권한 검사 설정

- [x] `addInterceptors()`에 `AdminInterceptor`를 등록한다.
- [x] Interceptor를 전체 경로에 등록하고, `@AdminOnly`가 붙은 컨트롤러 메서드에만 관리자 권한 검사를 적용한다.

---

## `AdminInterceptor`

### 관리자 페이지 및 API 접근 제한

- [x] `handler`가 `HandlerMethod`이고 메서드에 `@AdminOnly`가 있는지 먼저 확인한다.
- [x] 보호 대상인 경우에만 로그인 회원을 조회하고 관리자 권한을 검사한다.
- [x] 표시가 없는 메서드와 정적 리소스는 관리자 인증 없이 다음 처리로 진행한다.
- [x] `LoginMemberProvider`에 쿠키 추출·토큰 검증·로그인 회원 조회를 위임하고, 반환된 회원의 `role`을 확인한다.
- [x] 인증된 회원의 `role`이 `ADMIN`이 아니면 `403 Forbidden`을 설정하고 `false`를 반환한다.
- [x] 관리자이면 `true`를 반환해 컨트롤러 실행을 허용한다.

쿠키 누락이나 잘못된 토큰으로 발생한 인증 예외는 `ExceptionController`에서 `401`로 처리한다.

---

## `CookieTokenExtractor`

### 쿠키에서 토큰 추출

- [x] 요청의 쿠키 목록에서 이름이 `token`인 쿠키를 찾는다.
- [x] 토큰 값이 있으면 해당 문자열을 반환한다.
- [x] 쿠키가 없거나 `token` 쿠키가 없으면 `InvalidTokenException`을 발생시킨다.
- [x] 토큰 값이 `null`, 빈 문자열, 공백뿐인 경우에도 같은 예외를 발생시킨다.

---

## `JwtTokenProvider`

### JWT 생성

- [x] 로그인한 회원의 이메일을 JWT의 `subject`에 저장한다.
- [x] 발급 시각과 만료 시각을 설정한다.
- [x] 설정 파일의 비밀 키로 서명한 JWT를 반환한다.

### JWT 검증 및 이메일 추출

- [x] 토큰의 서명과 만료 여부를 검증한다.
- [x] 유효한 토큰의 `subject`에서 회원 이메일을 반환한다.
- [x] 토큰 검증에 실패하면 `InvalidTokenException`을 발생시킨다.

---

## `LoginMemberArgumentResolver`

### 로그인 회원 정보 주입

- [x] `HandlerMethodArgumentResolver`를 구현한다.
- [x] 컨트롤러 매개변수의 타입이 `LoginMember`인 경우 동작한다.
- [x] `LoginMemberProvider`에 쿠키 추출·토큰 검증·로그인 회원 조회를 위임한다.
- [x] 조회한 `LoginMember`를 컨트롤러 메서드의 인자로 전달한다.

---

## `MemberController`

### 회원가입

- [x] `POST /members`에서 `MemberRequest`를 전달받고 `@Valid`로 검증한다.
- [x] `MemberService`를 통해 회원을 생성한다.
- [x] `201 Created`, 생성된 회원의 `Location` 헤더, `MemberResponse`를 응답한다.

### 로그인

- [x] `POST /login`에서 `LoginRequest`의 이메일과 비밀번호를 전달받는다.
- [x] `@Valid`로 필수값을 검증하고 `MemberService`로 회원을 인증한다.
- [x] 인증한 회원의 이메일로 JWT를 생성한다.
- [x] JWT를 `token` 쿠키에 저장하고 `HttpOnly`, `Path=/`를 설정한다.
- [x] 성공 시 본문 없는 `200 OK`를 응답한다.

### 로그인 회원 조회

- [x] `GET /login/check`에서 Resolver가 전달한 `LoginMember`를 사용한다.
- [x] 회원의 `id`, `name`, `email`을 `MemberResponse`에 담아 응답한다.

### 로그아웃

- [x] `POST /logout`에서 `token` 쿠키 값을 비우고 `Max-Age=0`으로 설정한다.
- [x] 브라우저가 쿠키를 삭제하도록 응답하고 본문 없는 `200 OK`를 반환한다.

---

## `MemberService`

### 회원 생성 및 로그인 인증

- [x] 가입한 회원의 권한을 `USER`로 지정해 저장한다.
- [x] 이메일과 비밀번호가 일치하는 회원을 `MemberRepository`에서 조회한다.

### 로그인 회원 정보 조회

- [x] 이메일로 회원을 조회해 `LoginMember`를 생성한다.
- [x] 조회한 회원의 `id`, `name`, `email`, `role`을 전달한다.
- [x] 토큰의 이메일에 해당하는 회원이 없으면 `InvalidTokenException`을 발생시킨다.

---

## `MemberRepository`

### 회원 저장 및 조회

- [x] `JpaRepository<Member, Long>`의 `save()`로 회원을 저장하고 DB가 생성한 ID를 사용한다.
- [x] `findByEmailAndPassword()`로 로그인할 회원을 조회한다.
- [x] `findByEmail()`로 토큰의 이메일에 해당하는 회원을 조회한다.
- [x] `findByName()` 쿼리 메서드를 제공한다. 5단계의 이름 입력 예약은 회원 조회 없이 이름 자체를 저장한다.
- [x] 조회 결과가 없으면 `Optional.empty()`를 반환하며, 서비스에서 상황에 맞는 예외로 변환한다.

---

## `LoginMember`, `LoginRequest`, `MemberRequest`, `MemberResponse`

### 회원 요청·응답 데이터 관리

- [x] `LoginMember`에 인증된 회원의 `id`, `name`, `email`, `role`을 보관한다.
- [x] `LoginRequest`로 `email`, `password`를 받고 두 필드에 `@NotBlank`를 적용한다.
- [x] `MemberRequest`로 `name`, `email`, `password`를 받고 각 필드에 `@NotBlank`를 적용한다.
- [x] `MemberResponse`로 `id`, `name`, `email`을 응답한다.

---

## `ReservationController`

### 예약 생성

- [x] `POST /reservations`에서 `ReservationRequest`와 `LoginMember`를 전달받는다.
- [x] `@Valid`로 예약 요청의 필수값을 검증한다.
- [x] 요청 데이터와 로그인 회원 정보를 `ReservationService`에 전달한다.
- [x] 성공 시 `201 Created`, `/reservations/{id}`의 `Location` 헤더, 예약 정보를 응답한다.

### 예약 조회 및 삭제

- [x] `GET /reservations`로 전체 예약 목록을 응답한다.
- [x] `DELETE /reservations/{id}`로 예약을 삭제하고 본문 없는 `204 No Content`를 응답한다.

---

## `ReservationRequest`, `ReservationResponse`

### 예약 요청·응답 데이터 관리

- [x] `ReservationRequest`의 `name`은 선택값으로 받는다.
- [x] `date`에 `@NotBlank`, 테마 ID인 `theme`과 시간 ID인 `time`에 `@NotNull`을 적용한다.
- [x] `ReservationResponse`로 예약 ID, 예약자 이름, 테마 이름, 날짜, 시간을 응답한다.

---

## `ReservationService`

### 예약자 결정 및 예약 생성

- [x] 요청의 `name`이 생략되거나 `null`이면 `LoginMember`의 ID로 회원을 조회한다.
- [x] `name`이 있으면 이름 자체를 저장하고 회원과 연결하지 않는다.
- [x] 시간·테마와 선택적인 회원 연결을 가진 `Reservation`을 저장한다.
- [x] 저장 결과를 `ReservationResponse`로 변환한다.

빈 문자열이나 공백뿐인 이름은 거절한다. 이름을 지정할 때 별도의 관리자 권한 검사는 하지 않는다.

---

## `ReservationRepository`

### 예약 저장·조회·삭제

- [x] 예약 날짜와 회원·테마·시간의 외래 키를 저장한다.
- [x] 회원 연결 예약은 회원 이름을, 이름 입력 예약은 저장된 이름을 사용한다. 테마·시간은 연관 엔티티에서 읽는다.
- [x] `@EntityGraph`로 예약 목록과 회원·테마·시간을 함께 조회한다.
- [x] 날짜와 테마 ID에 해당하는 예약 목록을 조회한다.
- [x] 예약 ID를 기준으로 삭제한다.

---

## `ThemeController`, `ThemeService`, `ThemeRepository`, `Theme`

### 테마 관리

- [x] 테마를 생성하고 전체 목록을 조회하며 ID로 삭제한다.
- [x] 테마 생성 시 `@Valid`로 요청 데이터를 검증한다.
- [x] `name`에 `@NotBlank`, `description`에 `@NotNull`을 적용한다.
- [x] 테마 설명은 빈 문자열을 허용한다.
- [x] 삭제 시 `deleted=true`로 변경하고 목록에서 제외해 기존 예약의 참조를 보존한다.

---

## `TimeController`, `TimeService`, `TimeRepository`, `Time`, `AvailableTime`

### 시간 관리 및 예약 여부 조회

- [x] 시간을 생성하고 전체 목록을 조회하며 ID로 삭제한다.
- [x] 시간 생성 시 `@Valid`로 검증하고 `value`에 `@NotBlank`를 적용한다.
- [x] 날짜와 테마 ID를 기준으로 각 시간의 예약 여부를 확인한다.
- [x] 삭제되지 않은 시간에 대해 `timeId`, `time`, `booked`를 응답한다.
- [x] 시간 삭제도 논리 삭제로 처리해 기존 예약의 참조를 보존한다.

---

## `ExceptionController`, `InvalidTokenException`

### 인증 오류 및 요청 오류 응답

- [x] 쿠키 누락, 토큰 검증 실패, 토큰에 해당하는 회원 미존재를 `InvalidTokenException`으로 구분한다.
- [x] `InvalidTokenException` 발생 시 본문 없는 `401 Unauthorized`를 응답한다.
- [x] `ResponseStatusException`의 상태 코드를 유지해 대기 취소의 403·404를 응답한다.
- [x] 필수값 검증 실패 등 공통 예외 처리기에 전달된 나머지 예외는 본문 없는 `400 Bad Request`로 응답한다.

---

## JPA 저장 구조

- `Member`, `Theme`, `Time`, `Reservation`을 엔티티로 매핑하고 ID는 `IDENTITY`로 생성한다.
- 네 DAO를 `JpaRepository` 기반 Repository로 대체한다.
- 서비스의 `@Transactional` 범위에서 저장·삭제 및 응답 DTO 변환을 수행한다.
- 테마·시간은 변경 감지로 논리 삭제하고, 예약은 실제 삭제한다.
- Hibernate가 테이블을 생성하고 `data.sql`이 초기 데이터를 넣는다. 학습용 H2 메모리 DB와 `create-drop` 설정이므로 데이터는 재시작하면 초기화된다.
- `Time.getTime()`은 미션 예제의 Java 호출을 지원한다. `@JsonIgnore`로 기존 API의 `id`, `value` 응답을 유지한다.

## API 명세

요청 본문은 `application/json`으로 전송한다. 로그인이 필요한 요청에는 발급받은 쿠키를 포함한다.

```http
Cookie: token=<발급된 JWT>
```

### 회원 및 로그인

| 기능 | 메서드 | 경로 | 요청 본문 | 성공 응답 |
| --- | --- | --- | --- | --- |
| 회원가입 | POST | `/members` | `name`, `email`, `password` | `201`, 회원 정보, `Location` |
| 로그인 | POST | `/login` | `email`, `password` | `200`, `token` 쿠키 |
| 로그인 회원 조회 | GET | `/login/check` | 없음 | `200`, 회원 정보 |
| 로그아웃 | POST | `/logout` | 없음 | `200`, 쿠키 삭제 |

로그인 요청 예시:

```json
{
  "email": "admin@email.com",
  "password": "password"
}
```

로그인 회원 조회 응답 예시:

```json
{
  "id": 1,
  "name": "어드민",
  "email": "admin@email.com"
}
```

### 예약

| 기능 | 메서드 | 경로 | 요청 | 성공 응답 |
| --- | --- | --- | --- | --- |
| 내 예약 목록 조회 | GET | `/reservations-mine` | 로그인 쿠키 | `200`, 본인 예약 목록 |
| 예약 목록 조회 | GET | `/reservations` | 없음 | `200`, 예약 목록 |
| 예약 생성 | POST | `/reservations` | `date`, `theme`, `time`, 선택값 `name` | `201`, 예약 정보, `Location` |
| 예약 삭제 | DELETE | `/reservations/{id}` | 예약 ID | `204`, 본문 없음 |

예약 생성은 로그인이 필요하며, 이미 예약된 날짜·시간·테마 조합은 `400`으로 거절한다. `theme`, `time`에는 각각 테마와 시간의 ID를 전달한다.

예약자 이름을 생략한 요청 예시:

```json
{
  "date": "2026-09-20",
  "theme": 1,
  "time": 1
}
```

어드민으로 로그인한 경우의 응답 예시이며, `id`는 DB가 생성한 값이다.

```json
{
  "id": 4,
  "name": "어드민",
  "theme": "테마1",
  "date": "2026-09-20",
  "time": "10:00"
}
```

요청에 `"name": "브라운"`을 추가하면 회원 조회 없이 그 이름으로 예약한다. 이 예약은 브라운 회원의 내 예약 목록에 자동으로 포함되지 않는다.

### 내 예약 목록 (5단계)

- `GET /reservation-mine`은 기존 내 예약 화면을 반환한다.
- `GET /reservations-mine`은 쿠키로 확인한 회원 ID에 연결된 예약만 ID 오름차순으로 반환한다.
- `MyReservationResponse`는 `id`, `reservationId`, `theme`, `date`, `time`, `status`를 담는다. 확정 예약의 status는 `예약`, 대기는 `N번째 예약대기`다. 대기의 reservationId는 null이며 id가 취소할 대기 ID다.
- 본인의 확정 예약과 대기를 함께 반환한다. 둘 다 없으면 `200`과 빈 배열, 인증이 없거나 유효하지 않으면 `401`을 반환한다.
- 이름만 입력한 예약은 member_id가 null이므로 로그인 회원의 목록에 포함되지 않는다.
- 초기 데이터는 어드민 회원 예약 3건과 이름만 브라운인 예약 1건이다.
- [5단계 개념·코드·구현 순서](docs/step5-my-reservations.md)

### 예약 대기 (6단계)

| 기능 | 메서드 | 경로 | 요청 | 성공 응답 |
| --- | --- | --- | --- | --- |
| 예약 대기 신청 | POST | `/waitings` | `date`, `time`, `theme`, 로그인 쿠키 | `201`, `id`, `waitingNumber`, `Location` |
| 본인 대기 취소 | DELETE | `/waitings/{id}` | 대기 ID, 로그인 쿠키 | `204` |

- `Waiting`은 회원·날짜·시간·테마를 저장한다. 이미 예약된 시간대에만 신청 가능하며, 본인 확정 예약 및 본인의 중복 대기는 `400`으로 거절한다.
- `WaitingWithRank`와 Repository의 생성자 프로젝션 쿼리로 같은 시간대에서 먼저 생성된 대기 수를 계산한다. 표시 순번은 이 값 + 1이다.
- 앞 대기가 취소되면 다음 조회에서 순번이 줄어든다. 다른 날짜·시간·테마는 순번 계산에서 제외한다.
- 대기 취소는 소유자만 가능하다. 다른 회원은 `403`, 없는 대기는 `404`, 미인증은 `401`이다.
- 확정 예약은 `(date, time_id, theme_id)`, 대기는 `(member_id, date, time_id, theme_id)` DB 유일성 제약으로 동시 중복 저장도 막는다. 사전 중복 검사와 제약 위반은 현재 예외 처리 정책에서 `400`이다.
- 예약 취소 후 대기 자동 승급은 구현 범위에 포함하지 않는다.
- [6단계 개념·파일별 변경 이유·구현 순서](docs/step6-waiting.md)

### 테마 및 시간

| 기능 | 메서드 | 경로 | 요청 | 성공 응답 |
| --- | --- | --- | --- | --- |
| 테마 목록 조회 | GET | `/themes` | 없음 | `200`, 테마 목록 |
| 테마 생성 | POST | `/themes` | 본문: `name`, `description` | `201`, 테마 정보, `Location` |
| 테마 삭제 | DELETE | `/themes/{id}` | 테마 ID | `204`, 본문 없음 |
| 시간 목록 조회 | GET | `/times` | 없음 | `200`, 시간 목록 |
| 시간 생성 | POST | `/times` | 본문: `value` | `201`, 시간 정보, `Location` |
| 시간 삭제 | DELETE | `/times/{id}` | 시간 ID | `204`, 본문 없음 |
| 시간별 예약 여부 조회 | GET | `/available-times` | 쿼리: `date`, `themeId` | `200`, 시간별 예약 여부 목록 |

테마 응답은 `id`, `name`, `description`, 시간 응답은 `id`, `value`를 포함한다.
`GET /available-times?date=2026-09-20&themeId=1`은 삭제되지 않은 시간을 반환하며 각 항목은 다음 형태이다.

```json
{
  "timeId": 1,
  "time": "10:00",
  "booked": true
}
```

### 관리자 페이지·API 및 실패 응답

관리자 페이지와 관리 API의 메서드에 `@AdminOnly`를 붙여 보호한다.

| 관리자 전용 요청 | 기능 |
| --- | --- |
| `GET /admin`, `/admin/reservation`, `/admin/theme`, `/admin/time` | 관리자 화면 |
| `POST /themes`, `DELETE /themes/{id}` | 테마 생성·삭제 |
| `POST /times`, `DELETE /times/{id}` | 시간 생성·삭제 |
| `GET /reservations`, `DELETE /reservations/{id}` | 전체 예약 조회·삭제 |

`GET /themes`, `GET /times`, `GET /available-times`는 공개 조회를 유지한다.
`POST /reservations`는 일반 회원도 사용할 수 있으며, 기존 Resolver가 로그인 여부를 확인한다.
`@AdminOnly`는 런타임에 확인하는 메서드 전용 표시다. 새 관리자 전용 메서드에도 표시해야 보호된다.

| 요청 상황 | 응답 |
| --- | --- |
| `ADMIN` 회원이 관리자 페이지에 접근 | `200 OK` |
| 인증된 일반 회원이 관리자 페이지 또는 관리자 API에 접근 | `403 Forbidden` |
| 인증이 필요한 요청에서 쿠키 누락 또는 유효하지 않은 토큰 사용 | `401 Unauthorized` |
| 토큰의 이메일에 해당하는 회원이 없음 | `401 Unauthorized` |
| 로그인 이메일·비밀번호 불일치 | `400 Bad Request` |
| `@NotBlank`, `@NotNull` 조건을 위반한 요청 | `400 Bad Request` |

리뷰를 반영하여 인증 실패는 `401`, 인증 후 관리자 권한 부족은 `403`으로 구분한다.
미션에서 제공한 일반 회원의 관리자 접근 기대값 `401`도 `MissionStepTest.삼단계()`에서 `403`으로 변경했다.

`@Valid`에 의한 검증은 요청 처리 시 실행된다. 객체를 `new`로 생성하는 것만으로 검증되지는 않는다.

## 실행 및 테스트

Java 17 환경에서 실행한다.

```bash
./gradlew bootRun
```

초기 계정은 다음과 같다.

| 권한 | 이메일 | 비밀번호 |
| --- | --- | --- |
| ADMIN | `admin@email.com` | `password` |
| USER | `brown@email.com` | `password` |

1~3단계 요구사항 테스트:

```bash
./gradlew test --tests roomescape.MissionStepTest
```

테스트도 8080 포트를 사용하므로 실행 중인 애플리케이션을 종료한 뒤 실행한다.
