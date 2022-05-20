
# 엔티티 조회 시 성능 최적화
- 요구지식 : JPA 기본 사용법(연관관계 매핑, 영속성 컨텍스트의 동작 원리, JPQL, 페치조인...)
- 참고자료 : 인프런 김영한 님의 [실전! 스프링 부트와 JPA 활용2 - API 개발과 성능 최적화](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8-JPA-API%EA%B0%9C%EB%B0%9C-%EC%84%B1%EB%8A%A5%EC%B5%9C%EC%A0%81%ED%99%94)

---

## 1. OSIV 끄기, 커맨드와 쿼리를 분리하라
<details>
<summary>접기/펼치기 버튼</summary>
<div markdown="1">

### OSIV ON
- `spring.jpa.open-in-view` : true (기본값)
- 최초 데이터베이스 커넥션 시작시점부터, API 응답이 끝날 때까지 영속성 컨텍스트와 데이터베이스 커넥션 유지
  - ViewTemplate, API Controller에서 지연로딩 가능
  - 기본적으로 DB와 커넥션을 계속 유지
- 트랜잭션 범위 내에서는 영속 상태, 수정 가능.
- 트랜잭션 범위 밖에서는 영속 상태, 수정 불가능.

### OSIV ON - 단점
- 데이터베이스 커넥션 리소스를 사용하기 때문에 실시간 트래픽이 중요한 애플리케이션에서는 커넥션이 모자랄 수 있음.
  - 요컨대, 커넥션을 너무 오래 들고 있음
  - 장애의 주된 요인!!!

### OSIV OFF
- `spring.jpa.open-in-view` : false
- 트랜잭션을 종료할 때 영속성 컨텍스트를 닫고, 데이터베이스 커넥션도 반환. 커넥션 리소스 낭비 x
- 지연로딩을 트랜잭션 안에서 강제로딩해서 처리해야함.
  - 트랜잭션 밖에서 지연로딩을 사용하고 있었다면, 지연로딩 코드를 트랜잭션 안으로 넣어야함.
  - ViewTemplate, APIController에서 지연로딩이 동작하지 않음.

### OSIV와 성능 최적화 기본 전략
```java
    // 커맨드, 쿼리 분리의 예
    @PutMapping("/api/members/{id}")
    public UpdateMemberResponse updateMember(
            @PathVariable Long id,
            @RequestBody @Valid UpdateMemberRequest request) {
        memberService.update(id, request.getName(), request.getAddress()); // 커맨드

        Member updatedMember = memberService.findOne(id); // 쿼리
        return new UpdateMemberResponse(updatedMember);
    }
```
> Command와 Query를 분리하라.
- 비즈니스 로직(삽입, 수정, 삭제)은 보통 엔티티 몇 개를 등록하거나 수정하는 것이므로 성능에 크게 문제가 되지 않음.
- 조회는 매우 빈번하게 일어나는 것이고 요청에서 필요한 API 사양에 맞게 성능을 최적화하는 것이 매우 중요함. 하지만 엔티티의 상태 변경, 추가 등 핵심적인 비즈니스 로직을 수행하는 것은 아님.
- 복잡한 애플리케이션 개발에 있어, 커맨드와 쿼리라는 관심사를 명확히 분리하는 것이 유지보수 관점에서 유리
  - OrderService : 핵심 비즈니스 로직
  - OrderQueryService : 화면, API에 맞춘 서비스(주로 읽기 전용 트랜잭션 사용)
- 주로 고객 서비스의 실시간 API는 OSIV를 끄고, ADMIN처럼 커넥션을 많이 사용하지 않는 곳에서는 OSIV를 켜는 식으로 사용

</div>
</details>

## 2. 응답 시, 엔티티 대신 응답을 위한 DTO에 담아서 반환해야한다.
<details>
<summary>접기/펼치기 버튼</summary>
<div markdown="1">

- 만약 엔티티의 특정 프로퍼티명이 변경된다면, api 사양 자체가 변경된다. api를 사양하는 측에서는 엔티티 사양 변경에 영향을 바로 받게 되므로 매우 고통스러워진다.
- 일대다 양방향 참조 관계가 있다면, json 응답시 무한 상호참조 문제가 발생할 수 있음.
  - `@JsonIgnore` 옵션을 줘서 특정 방향에서 참조하는걸 막을 수는 있음.
  - 그렇지만 특정 api, 또는 view를 위한 코드가 엔티티가 갖게되는 문제가 발생함.
- 해결책 : 응답 api를 위한 DTO를 생성할 것. api 사양의 변경은 DTO의 변경을 통해서만 일어나야한다.
  - 참고 : 요청 api 역시 요청에 맞는 api를 생성할 것. spring validation을 사용한다면 요청 dto에서 검증을 위한 어노테이션을 두면 됨.

</div>
</details>

## 3. 로딩 전략은 기본적으로 지연로딩을 사용할 것!!!
<details>
<summary>접기/펼치기 버튼</summary>
<div markdown="1">

- `@xxTOMany` : 기본적으로 지연로딩 전략(`fetch = FetchType.LAZY`)을 사용
- `@xxTOOne` : 기본적으로 즉시로딩(`fetch = FetchType.EAGER`) 전략을 사용.
  - `@..ToOne`은 반드시 지연로딩으로 설정을 해줘야한다!!!
- 즉시로딩이 발생하면, 예상치 못한 SQL이 날아감.
  - 참조하고 있는 엔티티 내부에서도 EAGER 전략을 사용해서 가져오는 연관관계가 있다면 이들도 불필요하게 가져오게 됨.
  - 어떤 SQL이 날아가는지 예측하기 어렵다 = 쿼리 최적화를 할때 매우 힘들어진다
- 특정 엔티티를 함께 즉시 로딩하고 싶을 경우에는 페치 조인을 사용하면 SQL을 예측하기 편해짐.

</div>
</details>

## 4. ToOne 관계를 함께 조회하는 것이 필요하다면 페치조인
<details>
<summary>접기/펼치기 버튼</summary>
<div markdown="1">

### ToOne 관계를 지연로딩할 경우
```java
    @GetMapping("/api/v2/simple-orders")
    public SimpleOrderListResponse ordersV2() {
        List<Order> orderEntities = orderRepository.findAllByString(new OrderSearch());
        return SimpleOrderListResponse.create(orderEntities);
    }
```
- 쿼리가 총 1 + N + N번 실행됨(v1과 쿼리 수가 같다.)
  - order 조회 한 번
  - order -> Member 조회 N번
  - order -> Delivery 조회 N번
  - 예) order 결과가 4개면 최악의 경우 1+4+4번 실행됨(최악의 경우)
    - 참고) 지연로딩은 영속성 컨텍스트에서 조회하므로, 이미 조회된 경우 쿼리를 생략함

### 페치로딩 사용
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

## 5. 간단한 1:多 조인은 페치조인을 고려해볼만 하다.
<details>
<summary>접기/펼치기 버튼</summary>
<div markdown="1">

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
- 주의점 : 일대다 조인 결과 row의 갯수가 뻥튀기됨. 식별자 기준으로 중복 제거(distinct) 가능
  - JPQL의 distinct는 다음 두가지 기능을 제공한다.
  - SQL에 distinct를 추가하여 날림
    - 이것만으로는 행마다 내용이 다르므로, 완전히 중복이 제거되지 않음
  - 애플리케이션 단에서, 같은 식별자를 가진 엔티티의 중복을 제거한다.
- 한계
  - 페이징 불가능 : 하이버네이트는 경고 로그를 남기면서 모든 데이터를 DB에서 찾아오고 메모리에서 페치조인 해버림.(매우 위험함)
  - 여러개의 일대다 관계를 페치 조인하면 데이터가 부정합하게 조회될 수 있음
- 결론
  - 한 가지 일대다 연관관계를 가져올 때는 사용할만한 전략.
  - 하지만, 페이징이나, 2개 이상 연관관계를 조인해올 때는 사용하면 안 된다.

</div>
</details>

## 6. 복잡한 1:多 조인은 지연로딩을 하되, `default_batch_fetch_size`을 통해 최적화해서 가져온다.
<details>
<summary>접기/펼치기 버튼</summary>
<div markdown="1">

### 컬렉션을 페치조인을 하면 페이징이 불가능하다.
- 일대다조인 -> row 뻥튀기
- 페이징을 시도할 경우 하이버네이트는 메모리에 모든 데이터를 가져와서 페이징 시도 -> 장애 발생 가능성

### 그렇다고 1:多 관계를 지연로딩하자니..
- N+1 문제 발생
- 그래서 어쩌라고?!!

### 지연로딩 최적화
```java
    // 가져올 때는 toOne 관계만 페치조인
    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return em.createQuery(
                        "SELECT o FROM Order as o " +
                                "join fetch o.member as m " +
                                "join fetch o.delivery as d", Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
```
```yaml
spring:
  jpa:
    properties:
      hibernate:
        default_batch_fetch_size: 500
```
- 페이징 + 컬렉션 엔티티 함께 조회 방법
  - ToOne(ManyToOne, OneToOne)은 페치 조인
  - 컬렉션은 지연로딩으로 조회. (트랜잭션 안에서 필요한 것들을 지연로딩 시킨다.)
  - 지연로딩 성능 최적화를 위해 `spring.jpa.properties.hibernate.default_batch_fetch_size` 적용
    - 개별 최적화 : 일부 구간에만 적용하고 싶을 때는 `@BatchSize` 사용
    - 이 옵션은 컬렉션이나 프록시 객체를 조회 시 한번에 설정한 size만큼 IN 절로 식별자를 넣어 조회

### 지연로딩 최적화 시의 장점
- 장점 : 쿼리 호출 수가 N+1에서 1+1로 최적화
  - `1:m:n` 호출을 1+1+1 쿼리로 조회
  - 조인보다 DB 데이터 전송량이 최적화
    - 잘 정규화된 테이블이면 정규화해서 필요한 최소한의 데이터들만 가져온다.
    - Order와 OrderItem을 조인해서 조회하면 Order의 데이터가 OrderItem 갯수만큼 중복해서 조회되는데 지연로딩 방식을 사용하면, 각각 조회되므로 전송 중복데이터가 없어진다.
  - 페치 조인 방식과 비교했을 때, 쿼리 호출 수가 약간 증가하지만 DB 데이터 전송량이 감소
  - 컬렉션 페치 조인은 페이징이 불가능하지만, 이 방법은 페이징이 가능하다.

### 결론
- `@...TOOne`은 페이징에 영향을 주지 않으므로 페치 조인으로 쿼리 수를 줄이면 됨
- 나머지는 batchSize 조절을 통해 최적화
  - `default_batch_fetch_size`는 100~1000 사이 선택 권장.
    - 많이 잡을 경우 DB측 순간 부하 증가 가능성이 있음.
    - 애플리케이션단에서는 100이든 1000이든 결국 전체 데이터를 로딩해야하므로 메모리 사용량이 같다.
    - 1000으로 설정하는 것이 가장 성능상 좋지만, 결국 DB든 애플리케이션이든 순간 부하를 어느 정도까지 버틸 수 있을 지 성능 테스트를 거쳐서 결정하는 것이 좋음.

</div>
</details>

## 7. (심화) DTO로 조회하기 - 단건 조회 최적화
<details>
<summary>접기/펼치기 버튼</summary>
<div markdown="1">

```java
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    public OrderQueryDTOs findOrderQueryDTOs() {
        List<OrderQueryDTO> findOrders = findOrders(); // toOne 관계만 가져온다.
        findOrders.forEach(o-> o.setOrderItems(findOrderItems(o.getOrderId()))); // 컬렉션은 다시 조회해와서 setter주입
        return new OrderQueryDTOs(findOrders);
    }

    private List<OrderQueryDTO> findOrders() {
        return em.createQuery(
                        "SELECT " +
                                "new jpa.book.JPAShop.api.dto.OrderQueryDTO(o.id, m.name, o.orderDate, o.status, d.address) " +
                                "FROM Order as o " +
                                "join o.member as m " +
                                "join o.delivery as d", OrderQueryDTO.class)
                .getResultList();
    }

    private List<OrderItemQueryDTO> findOrderItems(Long orderId) {
        return em.createQuery(
                        "SELECT " +
                                "new jpa.book.JPAShop.api.dto.OrderItemQueryDTO(oi.order.id, i.name, oi.orderPrice, oi.count) "+
                                "FROM OrderItem as oi " +
                                "JOIN oi.item as i " +
                                "WHERE oi.order.id = :orderId", OrderItemQueryDTO.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

}
```
- Query : 루트 1번, 컬렉션 N번 실행 (N+1)
  - ToOne(N:1, 1:1)관계들을 먼저 조회하고, ToMany(1:N) 관계는 각각 별도로 처리한다.
    - ToOne 관계 조인은 row 수가 증가하지 않음
    - ToMany(1:N) 관계는 조인하면 row 수가 증가함
  - row 수가 증가하지 않는 ToOne 관계는 조인으로 최적화하기 쉬우므로 한 번 조회하고, ToMany 관계는 최적화하기 어려우므로 findOrderItems()와 같은 별도의 메서드로 조회

</div>
</details>

## 8. (심화) DTO로 조회하기 - 복수의 데이터 컬렉션 조회 최적화
<details>
<summary>접기/펼치기 버튼</summary>
<div markdown="1">

### 흐름
```java
    public OrderQueryDTOs findOrderQueryDTOs_Optimization() {
        List<OrderQueryDTO> findOrders = findOrders(); // toOne 관계만 DTO로 가져오기
        
        List<Long> orderIds = toOrderIds(findOrders); // stream돌려서, 식별자들만 가져오기
        Map<Long, List<OrderItemQueryDTO>> orderItemMap = findOrderItemMap(orderIds);

        findOrders.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));
        return new OrderQueryDTOs(findOrders);
    }
```
- toOrderIds : 조회된 주문들의 id 리스트
- orderItemMap : 각 id별 OrderItemQueryDTO
- forEach로, orderItemMap의 id에 해당하는 orderItems를 order에 set

### toOrderIds
```java
    private List<Long> toOrderIds(List<OrderQueryDTO> orderDTOs) {
        return orderDTOs.stream()
                .map(OrderQueryDTO::getOrderId)
                .collect(toList());
    }
```
- orderDTO 리스트를 탐색하여, id들의 List를 반환

### findOrderItemMap
```java
    private Map<Long, List<OrderItemQueryDTO>> findOrderItemMap(List<Long> orderIds) {
        return em.createQuery(
                        "SELECT " +
                                "new jpa.book.JPAShop.api.dto.OrderItemQueryDTO(oi.order.id, i.name, oi.orderPrice, oi.count) "+
                                "FROM OrderItem as oi " +
                                "JOIN oi.item as i " +
                                "WHERE oi.order.id in :orderIds", OrderItemQueryDTO.class)
                .setParameter("orderIds", orderIds)
                .getResultStream()
                .collect(groupingBy(OrderItemQueryDTO::getOrderId));
    }
```
- orderIds를 in절에 파라미터로 추가. 지정 리스트에 포함된 id들 중 같은 id값을 가진 것들만 조회
- 별도의 DTO로 OrderItemQueryDTO들 조회
- orderId를 구분 기준으로 하여, 그룹핑

### 최종연산
```java
findOrders.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));
```
- orderItemMap에 저장된 리스트를 찾아다 order들 각각에 주입

### 성능
- Query : 루트 1번, 컬렉션 1번
- ToOne 관계들을 모두 조회하고, 식별자 orderId들을 기반으로 toMany관계인 OrderItem들을 한번에 조회
- map을 이용해서 매칭 성능 향상(O(1))

</div>
</details>

## 8. (심화) DTO로 조회하기 - 복수의 데이터 컬렉션 조회 극한의 최적화 (페이징 안 됨)
<details>
<summary>접기/펼치기 버튼</summary>
<div markdown="1">

### flat 데이터 조회
```java
public List<OrderFlatDTO> findOrderQueryDTOs_flat() {
    return em.createQuery(
            "SELECT " +
                    "new jpa.book.JPAShop.api.dto.OrderFlatDTO" +
                    "(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count) " +
                    "FROM Order as o " +
                    "JOIN o.member as m " +
                    "JOIN o.delivery as d " +
                    "JOIN o.orderItems as oi " +
                    "JOIN oi.item as i", OrderFlatDTO.class)
            .getResultList();
}
```
- 한 방 쿼리로, 1:多 관계를 싹 List로 가져옴
### 그룹핑, 병합
```java
@GetMapping("/api/v6/orders")
public OrderQueryDTOs ordersV6() {
    List<OrderFlatDTO> flats = orderQueryRepository.findOrderQueryDTOs_flat();

    List<OrderQueryDTO> orders = flats.stream()
            .collect(
                    // 그룹핑 : 각각의 OrderQueryDTO를 기준으로 그룹핑
                    groupingBy(flat -> new OrderQueryDTO(flat.getOrderId(), flat.getCustomerName(), flat.getOrderDate(), flat.getOrderStatus(), flat.getAddress()),

                    // 수집대상 : OrderItemQueryDTO들을 List로 수집
                    mapping(flat -> new OrderItemQueryDTO(flat.getOrderId(), flat.getItemName(), flat.getOrderPrice(), flat.getCount()), toList())
            )).entrySet()
            .stream()
            .map(
                    // OrderQueryDTO에 List<OrderItemQueryDTO>를 추가한 OrderQueryDTO 생성
                    entry -> new OrderQueryDTO(entry.getKey().getOrderId(), entry.getKey().getCustomerName(), entry.getKey().getOrderDate(), entry.getKey().getOrderStatus(), entry.getKey().getAddress(), entry.getValue()))
            .collect(toList());
    return new OrderQueryDTOs(orders);
}
```
- 컬렉션을 제외한 필드들을 그룹핑 기준으로 삼고, 각각의 OrderItem들을 List로 수집
- 수집한 결과를 병합하는 별도의 코드를 작성

### 성능
- Query : 1번
- 단점
  - 쿼리는 한 번이지만, 1:多 조인으로 인해 DB에서 애플리케이션에 전달하는 데이터에 중복데이터가 추가되므로 상황에 따라 V5보다 느릴 수 있음
  - 애플리케이션단에서 그룹핑, 병합하는 추가 작업이 크다.
  - 페이징 불가(DB에서 가져올 때 뻥튀기 된 row로 가져오기 때문)

</div>
</details>

## 결론
1. 엔티티 조회 방식으로 우선 접근
  - toOne 관계는 페치 조인으로 쿼리 수 최적화
  - 컬렉션 최적화
    - 페이징이 필요하면 `default_batch_fetch_size`로 1+1 쿼리로 조회하기
    - 페이징이 필요 없으면, 페치조인 사용
2. 엔티티 조회 방식으로 해결이 안 되면(성능) DTO 조회 방식 사용
3. DTO 조회 방식으로 해결 안 되면, NativeSQL 또는 SpringJdbcTemplate 사용

---