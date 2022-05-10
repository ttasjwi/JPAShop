
# JPA SHOP
- 김영한 님의 '실전! 스프링 부트와 JPA 활용 1 - 웹 애플리케이션 개발'강의를 들으면서 따라치고, 배운 것들 정리

---

# 프로젝트 초기 설정
<details>
<summary>접기/펼치기 버튼</summary>
<div markdown="1">

## 의존 라이브러리
```groovy
dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
	implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
	implementation 'org.springframework.boot:spring-boot-starter-validation'
	implementation 'org.springframework.boot:spring-boot-starter-web'
	implementation 'org.springframework.boot:spring-boot-devtools'

	// 쿼리 parameter를 로그로 남김
	implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.8.0'
	compileOnly 'org.projectlombok:lombok'
	runtimeOnly 'com.h2database:h2'
	annotationProcessor 'org.projectlombok:lombok'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```
- `spring-web` : 웹
- `spring-devtools`
    - html 파일을 수정후 리컴파일만 해주면 서버 재시작 없이 view 파일 변경 가능
    - build - Recompile (Ctrl + shift + F9)
- thymeleaf : 템플릿엔진 타임리프
- `spring-data-jpa` : jpa
- `p6spy` : 쿼리 로그
- `lombok` : 각종 편의 어노테이션

## application.yml
```yaml
spring:
  datasource: # 데이터 소스
    url: jdbc:h2:tcp://localhost/~/jpashop # 실행 환경의 홈 디렉토리에 있는 jpashop을 uri로 함
    username: sa
    password:
    driver-class-name: org.h2.Driver

  jpa:
    hibernate:
      ddl-auto: create # 애플리케이션 실행 시점에 테이블을 drop하고, 다시 생성함.
    properties:
      hibernate:
#        show_sql: true # sout으로 하이버네이트 실행 SQL을 남기는 것인데, 후술할 logging으로 대체한다.
        format_sql: true # 보여지는 쿼리를 예쁘게 보여줌

logging:
  level:
    org.hibernate.SQL: debug # 하이버네이트 실행 SQL을 logger을 통해 남긴다.
    org.hibernate.type: trace  # 쿼리 parameter의 값을 로그로 남김. 배포환경에서는 사용하지 성능 상 문제가 있다면 사용할지 말지를 고민하는 것이 좋다.
```
- spring.datasource : 데이터 소스 설정
- spring.jpa.hibernate : 하이버네이트 설정
- logging.level : 로깅 레벨 설정
    - org.hibernate.SQL : 하이버네이트 실행 SQL을 logger로 남김
    - org.hibernate.type : 쿼리 paramter의 값을 로깅

</div>
</details>

---

# 회원 도메인 개발

## MemberRepository
<details>
<summary>접기/펼치기 버튼</summary>
<div markdown="1">

```java
@Repository
public class MemberRepository {

    @PersistenceContext
    private EntityManager em;
```
```java
@Repository
@RequiredArgsConstructor
public class MemberRepository {

    private final EntityManager em;
```
- `@Repository` : 스프링 빈으로 Repository 등록
- `@PersistenceContext` : 엔티티매니저 자동 주입
- 스프링데이터 JPA를 사용하면 EntityManager도 자동 의존관계주입 가능
- 기능
  - save : 회원 저장
  - findOne : 회원 조회(id로 단건 조회)
  - findAll : 회원 전체 조회
  - findByName : 이름으로 회원 조회

</div>
</details>

## MemberService
<details>
<summary>접기/펼치기 버튼</summary>
<div markdown="1">

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 읽기 전용
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * 회원 가입
     */
    @Transactional // 변경
    public Long join(Member member) {
        validateDuplicateMember(member);
        memberRepository.save(member);
        return member.getId();
    }
```
- `@Transactional` : 트랜잭션, 영속성 컨텍스트. `readOnly = false`가 디폴트
  - `readOnly=true`
    - 데이터의 변경이 없는 읽기 전용 메서드에 사용. 영속성 컨텍스트를 플러시하지 않으므로 약간 성능 향상(읽기 전용에는 다 적용)
  - 데이터베이스 드라이버가 지원하면 DB에서는 성능 향상

</div>
</details>

## MemberServiceTest
<details>
<summary>접기/펼치기 버튼</summary>
<div markdown="1">

```java
@SpringBootTest
@Transactional
class MemberServiceTest {
```
- `@SpringBootTest` : 스프링부트 연동 테스트
- `@Transactional` : 테스트 종료 후 롤백
  - 롤백시키고 싶지 않으면 메서드에 `@RollBack(false)` 넣어주기
```yaml
#spring:
#  datasource: # 데이터 소스
#    url: jdbc:h2:mem:test # 스프링부트는 기본적으로 인메모리 테스트 DB를 사용
#    username: sa
#    password:
#    driver-class-name: org.h2.Driver
#
#  jpa:
#    hibernate:
#      ddl-auto: create-drop # 애플리케이션 실행 시점에 테이블을 drop하고, 다시 생성한 뒤 종료시점에 drop (테스트 - 스프링부트 디폴트)
#    properties:
#      hibernate:
##        show_sql: true # sout으로 하이버네이트 실행 SQL을 남기는 것인데, 후술할 logging으로 대체한다.
#        format_sql: true # 보여지는 쿼리를 예쁘게 보여줌

logging:
  level:
    org.hibernate.SQL: debug # 하이버네이트 실행 SQL을 logger을 통해 남긴다.
#    org.hibernate.type: trace  # 쿼리 parameter의 값을 로그로 남김. 배포환경에서는 사용하지 성능 상 문제가 있다면 사용할지 말지를 고민하는 것이 좋다.
```
- 인 메모리 테스트
  - `test/resources/application.yml`를 우선적으로 읽음.
    - 스프링은 디폴트로 인메모리 db를 사용
    - ddl-auto : create-drop을 기본 옵션으로 사용(drop - create - drop)

</div>
</details>

---

# 상품 도메인 개발

## Item

<details>
<summary>접기/펼치기 버튼</summary>
<div markdown="1">

```java
    //== 비즈니스 로직 ==/

    /**
     * stock 증가
     */
    public void addStockQuantity(int quantity) {
        this.stockQuantity += quantity;
    }

    /**
     * stock 감소
     */
    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;

        if (restStock < 0) {
            throw new NotEnoughStockException("Need More Stock");
        }
        this.stockQuantity = restStock;
    }
```
- Item에 관한 비즈니스 로직은 Item 스스로가 책임질 수 있어야한다.
- Item의 상태 변화는 setter를 사용하기보다 의미있는 메서드를 작성하는 것이 객체지향적이다.

</div>
</details>

---
