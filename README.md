# spring-basic-roomescape-playground

---

## User Requirement

- 사용자는 로그인을 할 수 있다.
  - 로그인에 성공하면 상단바 우측의 Login 버튼이 사용자 이름으로 변한다.
  - 로그아웃하면 다시 Login 버튼이 노출된다.

## 1단계 - 로그인

- [x] `POST /login` 요청 시 `email`, `password`로 로그인한다.
  - [x] `email`, `password`로 멤버를 조회한다.
  - [x] 조회한 멤버 정보로 토큰을 생성한다.
  - [x] 응답 Cookie에 `token` 값으로 토큰을 포함한다.
  - [x] `email` 또는 `password`가 틀리면 `401 Unauthorized`로 응답한다.
- [x] `GET /login/check` 요청 시 Cookie를 이용하여 로그인한 사용자의 정보를 조회한다.
  - [x] Cookie에서 `token` 값을 추출한다.
  - [x] 토큰에서 멤버 식별자를 얻어 멤버를 조회하고 이름을 응답한다.
  - [x] Cookie가 없거나 `token` 값이 없으면 `401 Unauthorized`로 응답한다.

## 2단계 - 로그인 리팩터링

- [x] Cookie의 인증 정보로 멤버 객체를 만드는 로직을 `HandlerMethodArgumentResolver`로 분리한다.
  - [x] 컨트롤러 메서드에서 `LoginMember`를 주입받아 사용한다.
- [x] `POST /reservations` 요청 시 `name`이 없으면 Cookie의 로그인 정보를 활용한다.
  - [x] `name`이 있으면 기존대로 `name`으로 예약을 생성한다. (관리자)
  - [x] `name`이 없으면 로그인한 멤버의 이름으로 예약을 생성한다. (로그인 사용자)

## 3단계 - 관리자 기능

- [x] `/admin/**` 진입은 `ADMIN` 권한이 있는 사람만 할 수 있다.
  - [x] `HandlerInterceptor`를 활용하여 컨트롤러 진입 전에 Cookie의 role을 확인한다.
  - [x] 권한이 없는 경우 `401 Unauthorized`로 응답한다.

## 4단계 - JPA 전환

- [x] `spring-boot-starter-jdbc` 의존성을 `spring-boot-starter-data-jpa`로 대체한다.
- [x] `Time`, `Theme`, `Member`를 엔티티로 매핑한다.
- [x] `Reservation`은 `Time`, `Theme`를 `@ManyToOne`으로 연관관계 매핑한다.
- [x] Dao를 `JpaRepository`를 상속받는 Repository로 대체하고, 필요한 조회는 쿼리 메서드로 정의한다.
- [x] 스키마 생성은 Hibernate에 맡기고, `schema.sql`은 초기 데이터만 남긴 `data.sql`로 대체한다.

## 5단계 - 내 예약 목록 조회

- [x] `GET /reservations-mine` 요청 시 로그인한 회원의 예약 목록을 응답한다.
- [x] `Reservation`은 `Member`를 `@ManyToOne`으로 연관관계 매핑한다.

## 6단계 - 예약 대기 기능

- [x] `POST /waitings` 요청 시 예약 대기를 생성한다.
- [x] `DELETE /waitings/{id}` 요청 시 예약 대기를 취소한다. (본인의 대기만 취소 가능)
- [x] 내 예약 목록 조회 시 예약 대기 목록도 함께 응답한다.
- [x] 중복 예약(이미 예약·대기한 슬롯에 다시 대기)이 불가능하다. (`409 Conflict`)
- [x] (심화) 예약 대기 상태에 몇 번째 대기인지 표시한다. (`1번째 예약대기`)

## 7단계 - @Configuration

- [x] JWT 생성과 조회 로직을 `roomescape`와 같은 계층의 `auth` 패키지(`JwtUtils`)로 분리한다.
  - [x] `JwtUtils`는 `@Component` 없이 `@Configuration`의 `@Bean`으로 등록한다.
  - [x] 시크릿 키는 `application.properties`에서 주입받는다.
- [x] 불필요한 DB 접근을 최소화한다.
  - [x] 토큰에 회원 식별자와 이름을 담고, `LoginMember`는 토큰만으로 만든다.
  - [x] 권한 확인이 필요한 경우(`/admin/**`, 관리자 예약)에만 DB에서 회원을 조회한다.

## 8단계 - Profile과 Resource

- [x] `data.sql` 대신 애플리케이션 시작 시 데이터를 초기화하는 `CommandLineRunner`를 만든다.
  - [x] 운영용 `DataLoader`는 사용자 정보만 초기화한다. (`@Profile("!test")`)
  - [x] 테스트용 `TestDataLoader`는 테스트에 필요한 사전 값을 초기화한다. (`@Profile("test")`)
  - [x] 테스트는 `@ActiveProfiles("test")`로 테스트 프로파일을 활성화한다.
- [x] 토큰 생성에 필요한 비밀키 값을 `application.properties`로 분리한다.

---

## 서비스 정책

요구사항에 명시되지 않아 이번 구현에서 선택한 정책입니다.

- 예약과 예약 대기는 **오늘 이후의 날짜**에만 할 수 있다.
  - 예약은 앞으로의 날짜에 대해 하는 것이지 지난 날짜에 대해 하지 않는다고 판단했다.
  - 오늘 날짜는 당일 예약으로 보고 허용한다.
- 예약 대기는 **로그인한 회원 본인만** 생성한다.
  - 관리자가 `name`으로 예약을 대신 만들어주는 경로는 있지만, 남의 대기를 대신 걸어주는 경로는 두지 않았다.
- 삭제된 시간·테마는 예약과 대기에 선택할 수 없다.
- 회원의 **권한은 토큰이 아닌 DB에서** 확인한다.
  - 권한은 인가 판단의 근거라서, 이미 발급된 토큰의 값이 아니라 최신 상태를 기준으로 판단한다.
  - 이름은 변경 기능이 없어 토큰의 값을 그대로 사용한다.
