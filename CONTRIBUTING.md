# 개발 규칙

## 패키지 템플릿

기능 단위(package by feature)로 구성한다. 각 기능은 API, 유스케이스, 도메인 규칙, 영속성 코드를 함께 가진다.

```text
com.eum.banca
├─ common
│  ├─ config          # 여러 기능에서 함께 쓰는 Spring 설정
│  ├─ exception       # 공통 예외와 에러 응답
│  └─ web             # 여러 기능이 함께 쓰는 HTTP 규약
└─ product
   ├─ api             # ProductController, 요청/응답 DTO
   ├─ application     # 유스케이스 조립과 트랜잭션 처리
   ├─ domain          # Product, ProductQuota, 도메인 규칙, Repository 포트
   └─ infrastructure  # JPA 엔티티와 Repository 구현체
```

미래 기능을 빈 최상위 패키지로 미리 만들지 않는다. 첫 유스케이스를 시작할 때 기능 패키지를 추가한다.

## 의존 방향

기능 내부 의존 방향은 다음과 같이 고정한다.

```text
api -> application -> domain
                    <- infrastructure
```

- `domain`은 다른 계층과 Spring, JPA 같은 기술 구현을 참조하지 않는다.
- `application`은 유스케이스와 트랜잭션을 조립하며 `infrastructure` 구현체를 직접 참조하지 않는다.
- `infrastructure`는 `domain`이 정의한 저장소 포트를 구현한다.
- `api`는 HTTP 요청을 애플리케이션 유스케이스로 전달하며 영속성 구현에 직접 접근하지 않는다.
- 기능 패키지는 공통 규약이 필요할 때만 `common`을 참조한다. `common`은 어떤 기능 패키지도 참조하지 않는다.
- 기능 간 협력은 상대 기능의 공개 인터페이스를 통한다. 상대 기능의 `infrastructure`를 직접 참조하지 않는다.

두 번째 기능 패키지가 생기는 시점부터 이 규칙을 아키텍처 테스트로 검증한다.

## 네이밍

- Java 패키지는 소문자만 사용하고 언더스코어를 쓰지 않는다: `com.eum.banca.product.domain`.
- 클래스는 PascalCase, 메서드와 변수는 camelCase를 사용한다.
- Controller는 `Controller`, HTTP DTO는 `Request` 또는 `Response`로 끝낸다.
- 애플리케이션 서비스는 유스케이스를 드러낸다: `ProductQueryService`, `ProductQuotaService`.
- 도메인 객체에는 기술 접미사를 붙이지 않는다: `Product`, `ProductQuota`.
- 영속성 구현체는 역할을 명확히 쓴다: `ProductJpaRepository`, `ProductRepositoryAdapter`.
- ID는 `<feature>Id` 형식을 쓴다: `productId`, `applicationId`.
- 에러 코드는 대문자 스네이크 케이스를 쓴다: `PRODUCT_NOT_FOUND`, `PRODUCT_NOT_ON_SALE`.
- API 경로는 복수형 명사를 쓴다: `/api/products`, `/api/products/{productId}`.
- DB 테이블과 컬럼은 스네이크 케이스, 테이블은 복수형을 쓴다: `products`, `product_quotas`, `product_id`.
- Flyway 파일은 `V{번호}__{동사}_{명사}.sql` 형식을 쓴다: `V2__add_product_sale_status.sql`.

## 변경 작업 네이밍

- 브랜치는 Jira 이슈가 `PLAN READY`로 전환될 때 자동 생성한다.
- 브랜치명은 `<Jira 키>-<하이픈으로 연결한 이슈 요약>` 형식을 쓴다: `BNC-5-M0-프로젝트-초기-환경-구성`.
- 자동 생성된 브랜치 이름을 임의로 바꾸거나 같은 Jira 키로 별도 브랜치를 만들지 않는다.
- 커밋은 `<type>(<jira-key>): <summary>` 형식을 쓴다: `feat(BNC-12): add product query`.
- PR 제목은 `[<jira-key>] <summary>` 형식을 쓴다.
