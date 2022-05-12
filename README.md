
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

## ItemRepository
<details>
<summary>접기/펼치기 버튼</summary>
<div markdown="1">

```java
    public void save(Item item) {
        if (item.getId() == null) {
            em.persist(item);
        } else {
            em.merge(item);
        }
    }
```
- save
  - id가 없음 : 신규 상품
  - id가 있음 : 갱신?
- findOne : 단건 조회
- findAll : 전체 조회

</div>
</details>

## ItemService
<details>
<summary>접기/펼치기 버튼</summary>
<div markdown="1">

```java
    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }
```
- 단순히 Repository에 위임

</div>
</details>

---

# 주문 도메인 개발

## Order, OrderItem
<details>
<summary>접기/펼치기 버튼</summary>
<div markdown="1">

### Order, OrderItem 생성 로직
```java
//== 생성 메서드 ==//
public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
    Order order = new Order();
    order.setMember(member);
    order.setDelivery(delivery);

    for (OrderItem orderItem : orderItems) {
        order.addOrderItem(orderItem);
    }
    order.setStatus(OrderStatus.ORDER);
    order.setOrderDate(LocalDateTime.now());
    return order;
}
```
```java
//== 생성 메서드 ==//
public static OrderItem createOrderItem(Item item, int orderPrice, int count) {
    OrderItem orderItem = new OrderItem();
    orderItem.setItem(item);
    orderItem.setOrderPrice(orderPrice);
    orderItem.setCount(count);

    item.removeStock(count);
    return orderItem;
}
```
- 생성이 복잡한 클래스는 별도로 static 메서드로 생성
- 주문시 item의 상태도 변경된다.
  - item의 상태 변화가 의미있는 메서드로 정의되어 있어서, 관계 확인이 편리하다.

### Order, OrderItem 비즈니스 로직
```java
//== 비즈니스 로직 ==//

/**
 * 주문 취소
 */
public void cancleOrder() {
    if (delivery.getStatus() == DeliveryStatus.COMP) {
        throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
    }
    this.setStatus(OrderStatus.CANCEL);

    for (OrderItem orderItem : orderItems) {
        orderItem.cancel();
    }
}

/**
 * 전체 주문가격 조회
 */
public int getTotalPrice() {
    return orderItems.stream()
            .mapToInt(OrderItem::getTotalPrice)
            .sum();
}
```
```java
//== 비즈니스 로직 ==//

/**
 * 주문 취소
 */
public void cancel() {
    getItem().addStockQuantity(count);
}

/**
 * 주문상품 전체 가격 조회
 */
public int getTotalPrice() {
    return getOrderPrice() * getCount();
}
```
- 주문 취소
- 주문 가격 조회

</div>
</details>

## OrderRepository
<details>
<summary>접기/펼치기 버튼</summary>
<div markdown="1">

```java
@Repository
@RequiredArgsConstructor
public class OrderRepository {
    
    private final EntityManager em;
    
    public void save(Order order) {
        em.persist(order);
    }
    
    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }
    
    //TODO
    // public List<Order> findAll(OrderSearch orderSearch) {}
    
}
```
- 주문 등록
- 주문 단건 조회
- TODO : 주문목록 필터링 조회

</div>
</details>

## OrderService
<details>
<summary>접기/펼치기 버튼</summary>
<div markdown="1">

### 주문 생성
```java
    @Transactional
    public Long order(Long memberId, Long itemId, int count) {

        // 엔티티 조회
        Member member = memberRepository.findOne(memberId);
        Item item = itemRepository.findOne(itemId);

        // 배송정보 생성
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());
        delivery.setStatus(DeliveryStatus.READY);

        // 주문상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

        // 주문 생성
        Order order = Order.createOrder(member, delivery, orderItem);

        // 주문 저장
        orderRepository.save(order);
        return order.getId();
    }
```
### 주문 취소
```java
@Transactional
public void cancelOrder(Long orderId) {

    // 주문 엔티티 조회
    Order order = orderRepository.findOne(orderId);

    // 주문 취소
    order.cancelOrder();
}
```
- 도메인 모델 패턴 : 엔티티가 비즈니스 로직을 가지고, 객체 지향의 특성을 적극 활용하는 것 
  - 비즈니스 로직 대부분이 '도메인'에 있음.
  - 서비스 계층은 단순히 엔티티에 필요한 요청을 위임하는 역할
- 트랜잭션 스크립트 패턴 : 엔티티에 비즈니스 로직이 거의 없고, 서비스 계층에서 대부분의 비즈니스 로직을 처리
- 우열을 가릴 수 없고 실무에서는 양쪽을 모두 쓴다고는 함.
  - JPA 위주 개발의 경우 주로 도메인 모델 패턴
  - JdbcTemplate, MyBatis 위주 개발의 경우 주로 트랜잭션 스크립트 패턴

</div>
</details>

## OrderServiceTest
<details>
<summary>접기/펼치기 버튼</summary>
<div markdown="1">

```java
@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;
```
- 단순히 order 생성에 필요한 member, item은 em을 통해 가져오도록 함
- 서비스 로직 하나를 테스트하기 위해 DB까지 연동해서 갖고오는 관점에선 좋은 테스트가 아님. 단위 테스트를 지향하자.
  - 도메인 따로.
  - 레포지토리 따로.
  - 독립적으로 서비스 따로.

</div>
</details>

## OrderSearch
<details>
<summary>접기/펼치기 버튼</summary>
<div markdown="1">

```java
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
        List<Predicate> criteria = new ArrayList<>();

        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"),
                    orderSearch.getOrderStatus());
            criteria.add(status);
        }
        
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    cb.like(m.<String>get("name"), "%" +
                            orderSearch.getMemberName() + "%");
            criteria.add(name);
        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); // 최대 1000건
        return query.getResultList();
    }
```
- JPA Criteria는 JPA 표준 스펙이지만 실무에서 사용하기에 너무 복잡
- QueryDSL로 동적 쿼리를 다뤄보자.

</div>
</details>

---

# 회원 웹 계층 개발

```html
<tr th:each="member : ${members}">
    <td th:text="${member.id}"></td>
    <td th:text="${member.name}"></td>
    <td th:text="${member.address?.city}"></td>
    <td th:text="${member.address?.street}"></td>
    <td th:text="${member.address?.zipcode}"></td>
</tr>
```
- `member.address?.city` : ?는 address가 null일 경우 무시하겠다는 뜻.


---

# 상품 웹 계층 개발

## 준영속 엔티티의 수정

### 준영속 엔티티
- 영속성 컨텍스트가 더 이상 관리하지 않는 엔티티

### 준영속 엔티티를 수정하는 방법
- 변경 감지 기능 사용
  - 영속성 컨텍스트에 영속화한 뒤 변경
  - DB에서 조회해서 영속성 컨텍스트에 가져온 뒤 변경
- 병합 사용
  - 준영속 엔티티의 식별자 값을 통해 DB에서 영속성 컨텍스트로 엔티티를 가져옴(select)
  - merge() 호출 시 준영속 엔티티의 모든 필드값을 덮어씀(Update)
  - 영속 엔티티를 반환

### merge의 위험성
- 변경감지 기능은 원하는 필드값만 선택해서 변경하면 됨
- 하지만 merge는 모든 필드값을 덮어씌움. 준영속 엔티티에 특정 필드 값이 없으면 null로 덮어씀

### 가장 좋은 해결 방법
> 엔티티를 변경할 때는 항상 변경감지를 사용한다.

- 컨트롤러에서 어설프게 준영속 엔티티를 생성하지 않는다.
- 트랜잭션이 있는 서비스 계층에 식별자와, 변경 데이터를 명확히 전달(파라미터 또는 dto)
- 트랜잭션이 있는 서비스 계층에서 식별자를 통해 영속 상태의 엔티티를 조회(SELECT)하고, 엔티티의 데이터를 직접 변경하라.
- 엔티티에는 무분별한 setter를 두지 않고 의미있는 이름으로 수정 메서드를 작성하라.
- 트랜잭션 커밋 시점에 변겨 감지가 실행되어, update 쿼리가 날아간다.

---

# 주문 웹 계층 개발
```java
    @PostMapping
    public String order(
            @RequestParam("memberId") Long memberId,
            @RequestParam("itemId") Long itemId,
            @RequestParam("count") int count) {
        orderService.order(memberId, itemId, count);
        return "redirect:/";
    }
```
- member, item이 필요한 상황인데, 이들 엔티티 조회는 영속성 컨텍스트 관리 하에서 하는 것이 좋다.

---

# 회원 웹 API 개발

## 회원 등록 API
<details>
<summary>접기/펼치기 버튼</summary>
<div markdown="1">

### 회원 등록 V1
```java

    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long memberId = memberService.join(member);
        return new CreateMemberResponse(memberId);
    }

    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }
```
- 파라미터에 엔티티가 바로 존재
- 이후 엔티티가 변경되면 요청 api 사양이 계속 변하는 문제 발생

### 회원등록 V2
```java
@PostMapping("/api/v2/members")
public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Member member = request.toEntity();
        Long memberId = memberService.join(member);
        return new CreateMemberResponse(memberId);
        }

@Data
static class CreateMemberRequest {
  @NotEmpty
  private String name;
  private String city;
  private String street;
  private String zipcode;

  public Member toEntity() {
    Member member = new Member();
    member.setName(name);
    member.setAddress(new Address(city,street, zipcode));
    return member;
  }
}
```
- 요청 API를 바인딩할 클래스를 별도로 정의
- 검증 어노테이션을 요청DTO에

</div>
</details>

## 회원 수정 API
```java
@PutMapping("/api/v2/members/{id}")
public UpdateMemberResponse updateMemberV2(
        @PathVariable Long id,
        @RequestBody @Valid UpdateMemberRequest request) {

    memberService.update(id, request.getName(), request.getAddress());

    Member updatedMember = memberService.findOne(id);
    return new UpdateMemberResponse(updatedMember);
}
```
- 쿼리와 커맨드를 분리하라.
  - 변경 결과 엔티티를 반환하는 행위는 커맨드, 쿼리의 역할을 동시에 수행하는 상황임
  - 하나의 메서드는 하나의 역할만.
- 위의 경우에서도, update메서드는 변경만 수행함. 엔티티를 반환하지 않음.

---