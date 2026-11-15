## Step 7
### Before Start
`RoomescapeException.userId`, `RoomescapeException.rejectedValues`는 로그를 위한 값이었으나, 일관적 적용을 하기 힘들며, 대부분의 경우 예외 발생 로그를 읽을 만한 가치 판단이 애매하여, 해당 부분을 삭제합니다.
단, 예외적으로 `ReservationController:106`, `RoleInterceptor:50`은 예외 파악을 위한 로그를 추가합니다.
### Requirement
- 불필요한 DB 접근을 최소화하세요.

현재 DB에 접근하는 경우를 나열하자면,
- `AuthService.login()`
  - email과 password. 두 컬럼이 완전히 일치하는 레코드 AuthRepository.findByEmailAndPassword()를 이용해 찾는다.

- `MemberService.createMember()`
  - 멤버 생성을 진행하기 전에 `MemberRepository.existsByEmail()`를 통해 중복 이메일 체크를 진행한다.
  - 멤버 생성을 진행하기 전에 `MemberRepository.existsByNickname()`을 통해 중복 닉네임 체크를 진행한다.
  - unique 제약 조건에 위배되지 않음을 확인하면 `MemberRepository.save()`한다.

- `MemberService.determineDuplicate`
  - `MemberController.createMember()`에서는 `MemberService.createMember()`에서 핸들링되지 않은 동시 요청으로 인한 unique 제약 조건 위배를 잡는다. 단, 이 동시성 제어는 극히 드문 상황에서만 일어나므로 DB 접근은 거의 일어나지 않는다.
    - 단, 로그만을 위해 개방한 메소드이므로, 존재 의의가 미약. 삭제

- `ReservationService.createReservation()`
  - 내부 조회 3개는 JPA 철학에 맞게 관계를 객체 주입으로 지정하기 위함
  - `MemberRepository.findByNickname()`
    - 기존 요구사항이었던 관리자의 경우 이름으로 예약하고, 일반 사용자인 경우 로그인 정보를 통해 예약 가능을 구현하기 위한 메소드.
      - name(실명) -> nickname(별명)으로 변경한 이유는 실명은 중복 가능한 리소스이지만, 별명은 중복 가능하지 않은 리소스임을 미미하게나마 포함함. unique 속성인 컬럼을 where절 필터 조건으로 제공
  - `ThemeRepository.findById()`
  - `TimeRepository.findById()`
  - 불필요 여부 : DB 네트워크 접속을 3회 진행합니다. 또한, member, theme, time의 다른 필드는 필요하지 않습니다. 단순 연관관계 설정에는 무겁습니다.
    - `ReservationRepository`를 `ListCrudRepository<>`에서 `JpaRepository<>`로 변경하고, `getReferenceById()` 조회로 ID를 가진 프록시 객체만 조회합니다.
    - ByAdmin, ByUser로 분리하여 Admin의 경우 실제 데이터를 조회하고, User의 경우, id를 통해 프록시 객체 주입하도록 분리
    - 프록시 객체 기반으로 작업하므로, 정합성 문제는 `@Transactional` 범위 바깥, 컨트롤러에서 DataIntegrityViolationException을 catch한 후, 위배한 제약 조건의 이름을 if문으로 필터링합니다.

- `ReservationService.deleteById()`
  - 관리자가 직접 예약을 취소하는 경우이므로 리소스 소유권은 판단하지 않음.

- `ReservationService.findAll()`
  - 페이지네이션 조회 등의 기타 요구사항이 현재 확인되지 않으므로, findAll()을 유지합니다.

- `ThemeService.saveTheme()`
  - `ThemeRepository.existsByName()` name 필드의 unique 제약 조건 위배 여부를 검사하는 쿼리입니다. 
    - 검사 대비 얻는 이득이 적으며, 완전히 동일한 문제 상황으로 ThemeController에서 동시성 제어를 진행하므로 삭제합니다.

- `ThemeService.deleteTheme()`
  - `Reservation.existsTheme()` 본 쿼리로 얻는 이득이 사실상 전무(관계가 있음을 명시하는 것 외의 목적 X) 또한, 예약 대기 등 다른 필터링은 존재하지 않았음.
    - 컨트롤러에서 예외를 잡는 것으로 수정

- `TimeService.getAvailableTime()`, `TimeService.findAll()`
  - 불필요 쿼리 없다고 판단됨.

- `TimeService.save()`
  - `TimeRepository.existsByTimeValue()` Theme에서와 마찬가지로 Controller에서 DataIntegrityViolationException으로 잡도록 삭제

- `TimeService.deleteById()`
  - 올바른 값으로 요청했는지 찾는 SELECT 쿼리를 추가

- `ReserveWaitingService`
  - getTime, getTheme 모두 DB 왕복하여 레코드 전체 조회중 -> getReferenceById로 수정
  - 프록시 객체로 변경함에 따라 컨트롤러에 try - catch 보강
  - 이외에 불필요 쿼리 없다고 판단됨.

## Step 8
`schema.sql`과 같은 src/main/resources 하위의 sql 파일 기반 세팅에서 SchemaInitializer로 DDL을, DataLoading로 DML을 애플리케이션 부팅 단계에서 주입하도록 변경

## Step 9
ubuntu 26.04 버전을 기준으로 
1. java 17 유무 확인
  1-1. java -version으로 java 유무가 확인되지 않았을 때엔 jdk 17 설치
2. `./gradlew clean test bootJar --no-daemon`으로 테스트 실행
   2-1. 테스트 실패 시 종료
3. h2 유무 확인
4. jar 방식으로 프로젝트 빌드 
5. 8080 port 점유 중인 프로세스 유무 확인
6. Spring boot 애플리케이션을 `application-prod.properties` 설정 파일을 주입하여 실행
