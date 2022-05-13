
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
<details>
<summary>접기/펼치기 버튼</summary>
<div markdown="1">

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

</div>
</details>

## 회원 목록 API
<details>
<summary>접기/펼치기 버튼</summary>
<div markdown="1">


### 회원 목록 V1 : 응답 값으로 엔티티를 외부에 직접 노출
```java
    @GetMapping("/api/v1/members")
    public List<Member> members() {
        List<Member> members = memberService.findMembers();
        return members;
    }
```
- 문제점
  - 엔티티에 프레젠테이션 계층을 위한 로직이 추가됨
  - 기본적으로 엔티티의 모든 값이 노출됨
  - 엔티티에 응답 스펙을 맞추기 위한 로직이 추가됨(`@JsonIgnore`, 별도의 뷰 로직 등...)
  - 실무에서는 같은 엔티티에 대해 API가 용도에 따라 다양하게 만들어지는데, 한 엔티티에 각각의 API를 위한 프레젠테이션 응답 로직을 담기는 어렵다.
  - 엔티티가 변경되면 API 스펙이 변한다.
  - 추가로, 컬렉션을 직접 반환하면 향후 API 스펙을 변경하기 어렵다. (별도의 Result 클래스 생성으로 해결)
- 결론
  - API 응답 스펙에 맞추어 별도의 DTO를 생성한다.

## 회원 목록 V2 : 응답 DTO를 별도로 생성하여 반환
```java
@GetMapping("/api/v2/members")
public MemberListResponse membersV2() {
    List<Member> findMemberEntities = memberService.findMembers();
    return MemberListResponse.create(findMemberEntities);
}
```
```java
@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberListResponse {

    private final List<MemberListElement> members;

    public static MemberListResponse create(List<Member> memberEntities) {
        List<MemberListElement> members = memberEntities.stream()
                .map(MemberListElement::new)
                .collect(Collectors.toList());

        return new MemberListResponse(members);
    }
```
- 엔티티를 DTO로 변환해서 반환
- 엔티티가 변해도 API 스펙이 변경되지 않는다.
- 컬렉션을 외부 클래스로 감싸서, 향후 필요한 필드를 추가할 수 있다.

</div>
</details>

---

# 간단한 주문 조회 웹 API 개발

## 간단한 주문조회 V1 : 엔티티를 직접 노출
<details>
<summary>접기/펼치기 버튼</summary>
<div markdown="1">

```groovy
implementation 'com.fasterxml.jackson.datatype:jackson-datatype-hibernate5'
```
```java
@SpringBootApplication
public class JpaShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(JpaShopApplication.class, args);
	}

	@Bean
	Hibernate5Module hibernate5Module() {
		Hibernate5Module hibernate5Module = new Hibernate5Module();

//		// 강제 지연로딩 설정
//		hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true);
		return hibernate5Module;
	}

}
```
- 강제 지연로딩을 목적으로 하이버네이트 모듈 사용
- Hibernate5Module 빈 등록
  - configure에서 FORCE_LAZY_LOADING 을 true로 설정하면, json 생성시 모든 프로퍼티를 강제 지연로딩시킴
  - 이때 반대편에서 호출하는 것을 막기 위해, 반대편 연관관계쪽에 `@JsonIgnore`를 달아야함
```java
@GetMapping("/api/v1/simple-orders")
public List<Order> ordersV1() {
    List<Order> all = orderRepository.findAllByString(new OrderSearch());
    for (Order order : all) {
        order.getMember().getName(); // Lazy 강제 초기화
        order.getDelivery().getStatus(); // Lazy 강제 초기화
    }

    return all;
}
```
- Member, Delivery 정보를 응답으로 보내기 위해, 메서드 호출하여 강제 지연로딩시킴.

### V1 문제점
- 엔티티를 직접 응답으로 외부 노출하므로, 엔티티 변경 시 API가 변경됨
- 엔티티에서 `@JsonIgnore` 설정을 건들게 되므로 다른 사용처에서도 영향
- 만약 즉시로딩(EAGER) 설정까지 사용할 경우 다른 곳에서도 불필요하게 즉시로딩됨.
- 성능 튜닝이 매우 어려워진다.
- 따라서, 항상 지연로딩을 기본으로 하고, 성능 최적화가 필요할 경우에는 페치조인(Fetch Join)을 사용하자.

</div>
</details>

## 간단한 주문 조회 V2 : 엔티티를 DTO로 반환
<details>
<summary>접기/펼치기 버튼</summary>
<div markdown="1">

### SimpleOrderListResponse : 간단한 주문 목록 응답
```java
@RequiredArgsConstructor
@Data
public class SimpleOrderListResponse {

    private final List<SimpleOrderListElement> orders;

    public static SimpleOrderListResponse create(List<Order> orderEntities) {
        List<SimpleOrderListElement> orders = orderEntities.stream()
                .map(SimpleOrderListElement::new)
                .collect(Collectors.toList());
        return new SimpleOrderListResponse(orders);
    }

    @Data
    static class SimpleOrderListElement {

        private Long orderId;
        private String customerName;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
```
- Order 엔티티를 `OrderListElement`로 변환, 이들의 리스트를 기반으로 `SimpleOrderListResponse`를 생성
```java
    @GetMapping("/api/v2/simple-orders")
    public SimpleOrderListResponse ordersV2() {
        List<Order> orderEntities = orderRepository.findAllByString(new OrderSearch());
        return SimpleOrderListResponse.create(orderEntities);
    }
```
- 응답 객체를 생성하여 반환

### V2 - 한계
- 쿼리가 총 1 + N + N번 실행됨(v1과 쿼리 수가 같다.)
  - order 조회 한 번
  - order -> Member 조회 N번
  - order -> Delivery 조회 N번
  - 예) order 결과가 4개면 최악의 경우 1+4+4번 실행됨(최악의 경우)
    - 지연로딩은 영속성 컨텍스트에서 조회하므로, 이미 조회된 경우 쿼리를 생략함

</div>
</details>

## 간단한 주문 조회 V3 : 엔티티를 DTO로 변환 - 페치 조인 최적화
<details>
<summary>접기/펼치기 버튼</summary>
<div markdown="1">

### OrderRepository
```java
public List<Order> findAllWithMemberDelivery() {
    return em.createQuery(
                    "SELECT o FROM Order as o " +
                            "join fetch o.member as m " +
                            "join fetch o.delivery as d", Order.class
            )
            .getResultList();
}
```
- 엔티티를 페치 조인(fetch Join)으로 쿼리 1번에 조회
- 페치 조인으로 order->member, order->delivery는 한번에 가져와지므로 지연로딩 x

</div>
</details>

## 간단한 주문 조회 V4 : API 명세에 맞는 데이터만 별도의 DTO로 가져오기

<details>
<summary>접기/펼치기 버튼</summary>
<div markdown="1">

### SimpleOrderQueryDto
```java
@Data
public class SimpleOrderQueryDto {

    private Long orderId;
    private String customerName;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;

    public SimpleOrderQueryDto(Long orderId, String customerName, LocalDateTime orderDate, OrderStatus orderStatus, Address address) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;
    }
}
```
- 쿼리로 가져온 결과를 바인딩할 DTO 생성

### SimpleOrderQueryRepository
```java
@Repository
@RequiredArgsConstructor
public class SimpleOrderQueryRepository {

    private final EntityManager em;
    public List<SimpleOrderQueryDto> findOrderDtos() {
        return em.createQuery(
                        "SELECT " +
                                "new jpa.book.JPAShop.api.dto.SimpleOrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address) " +
                                "from Order as o join o.member as m join o.delivery d", SimpleOrderQueryDto.class)
                .getResultList();
    }
}
```

- 일반 SQL을 사용할 때처럼, 원하는 값을 선택해서 조회
- SELECT 절에서 원하는 데이터를 직접 선택하므로 데이터베이스 - 애플리케이션 네트워크 용량 최적화(생각보다 그렇게 효과가 크지는 않음)
- Repository 재사용성이 떨어진다. 단순히 API 스펙에 맞춘 코드가 Repository에 들어가게 됨.
  - 해결책 : 엔티티를 조회하는 Repository와, API로 전달할 DTO를 조회하는 로직을 별도로 분리
- 재사용성이 많아진다면 엔티티로 조회하는게 더 나은 선택일 수 있다.
- 하지만 한 건 한 건에 담긴 데이터가 많을 경우 성능 최적화를 위해 DTO로 조회하는걸 고려해볼 필요가 있다. 중요한건 성능 테스트!

### 결론
엔티티를 DTO로 변환하거나, DTO로 바로 변환하는 방법 두가지 모두 장단점이 존재.
- 엔티티로 조회 : Repository 재사용성 증가, 개발 단순화
- DTO로 조회 : 엔티티로 조회하는 것의 성능 이슈가 있을 경우, 필요한 데이터만 가져오므로 성능 최적화
- 쿼리 방식 선택 권장 순서
  1. 우선 엔티티를 조회해오고, DTO로 변환하는 방법을 선택
  2. 필요하면 페치 조인으로 성능을 최적화 -> 여기까지 하면 대부분 성능 이슈가 해결
  3. 그래도 안 되면 DTO로 직접 조회하는 방법 사용
  4. 최후의 보루 : JPA가 제공하는 NativeSQL 또는, 스프링 JDBC Template을 사용하여 SQL 직접 사용

</div>
</details>

---

# 주문 조회 웹 API 개발 - 컬렉션 조회

## 주문 조회 V1 : 엔티티 직접 노출
<details>
<summary>접기/펼치기 버튼</summary>
<div markdown="1">

```java
@GetMapping("/api/v1/orders")
public List<Order> ordersV1() {
    List<Order> all = orderRepository.findAllByString(new OrderSearch());
    for (Order order : all) {
        order.getMember().getName();
        order.getDelivery().getStatus();
        List<OrderItem> orderItems = order.getOrderItems();
        orderItems.stream().forEach(o->o.getItem().getName());
    }
    return all;
}
```
- order 엔티티를 직접 조회
- 지연로딩 엔티티 직접 호출하여, 강제 로딩
- 엔티티를 직접 노출하므로 좋은 방법이 아니다.

</div>
</details>

## 주문 조회 V2 : 엔티티를 DTO로 변환
<details>
<summary>접기/펼치기 버튼</summary>
<div markdown="1">

```java
@Data
@RequiredArgsConstructor
public class OrderDTOs {

  private final List<OrderDTO> orders;

  public static OrderDTOs create(List<Order> orderEntities) {
    List<OrderDTO> orders = orderEntities.stream()
            .map(OrderDTO::new)
            .collect(Collectors.toList());

    return new OrderDTOs(orders);
  }

  @Data
  static class OrderDTO {

    private Long orderId;
    private String customerName;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;
    private List<OrderItemDTO> orderItems;

    public OrderDTO(Order orderEntity) {
      orderId = orderEntity.getId();
      customerName = orderEntity.getMember().getName();
      orderDate = orderEntity.getOrderDate();
      orderStatus = orderEntity.getStatus();
      address = orderEntity.getDelivery().getAddress();
      orderItems = initOrderItems(orderEntity.getOrderItems());
    }

    private List<OrderItemDTO> initOrderItems(List<OrderItem> orderItems) {
      return orderItems.stream()
              .map(OrderItemDTO::new)
              .collect(Collectors.toList());
    }

    @Data
    static class OrderItemDTO {

      private String itemName;
      private int orderPrice; // 주문 가격
      private int count; // 수량1

      public OrderItemDTO(OrderItem orderItem) {
        itemName = orderItem.getItem().getName();
        orderPrice = orderItem.getOrderPrice();
        count = orderItem.getCount();
      }
    }
  }
}
```
- 엔티티를 DTO로 변환
- DTO 내부에 엔티티를 그대로 넘겨선 안 된다. 내부 엔티티도 새로운 DTO로 만들어야한다.

### 성능 상의 문제점
- 지연 로딩으로 너무 많은 SQL 실행
- SQL 실행수(최악의 경우)
  - order : 1번
  - member : N번(order 조회 수만큼)
  - delivery.address : N번(delivery 조회 수 만큼)
  - orderItem : N번(order 조회수 만큼)
  - item : N번(order 조회수 만큼)
- 이미 조회된 엔티티가 영속성 컨텍스트에 있을 경우 엔티티를 사용, 없으면 SQL 날림

</div>
</details>

## 주문 조회 V3 : 페치조인 최적화
```java
public List<Order> findAllWithItem() {
    return em.createQuery(
            "SELECT distinct o FROM Order as o " +
                    "join fetch o.member as m " +
                    "join fetch o.delivery as d " +
                    "join fetch o.orderItems as oi " +
                    "join fetch oi.item as i ", Order.class)
            .getResultList();
}

```
- join fetch로 컬렉션을 페치조인. SQL 한 번만 실행됨
- 주의점 : 일대다 조인 결과 row의 갯수가 뻥튀기됨. 식별자 기준으로 중복 제거 가능
- 한계
  - 페이징 불가능 : 하이버네이트는 경고 로그를 남기면서 모든 데이터를 DB에서 찾아오고 메모리에서 페치조인 해버림.(매우 위험함)
  - 여러개의 일대다 관계를 페치 조인하면 데이터가 부정합하게 조회될 수 있음

---

