# 테스트 전략

## 기본 원칙

- 테스트는 서로 실행 순서나 다른 테스트의 데이터에 의존하지 않습니다.
- 실제 운영 환경과 다른 동작을 숨길 수 있으므로 MySQL 영속성 테스트에 H2를 사용하지 않습니다.
- 단위 테스트에는 Spring 컨텍스트와 Testcontainers를 사용하지 않습니다.
- 저장소, Flyway, 트랜잭션처럼 MySQL 동작에 의존하는 테스트는 MySQL Testcontainers로 검증합니다.
- 동시성 테스트와 부하 테스트는 일반 빌드에서 분리합니다.

## 테스트 종류와 책임

| 종류 | 검증 대상 | 도구와 실행 위치 |
| --- | --- | --- |
| 단위 테스트 | 도메인 규칙, 값 객체, 상태 전이 | JUnit 5와 AssertJ, `test` 작업 |
| 웹 테스트 | 요청 변환, Validation, 응답과 예외 매핑 | MockMvc 또는 `@WebMvcTest`, `test` 작업 |
| 통합 테스트 | Flyway, 저장소 쿼리, 제약조건, 트랜잭션 | MySQL Testcontainers와 `@IntegrationTest`, `integrationTest` 작업 |
| 동시성 테스트 | 잠금, 경합, 재시도와 정합성 | `@Tag("concurrency")`, `concurrencyTest` 작업 |
| 부하 테스트 | 처리량, 지연시간, 자원 사용량 | `@Tag("load")`, `loadTest` 작업 또는 별도 도구와 CI 잡 |

단위 테스트는 테스트 대상 객체를 직접 생성합니다. 도메인 규칙을 검증하기 위해 `@SpringBootTest`, MockMvc, 데이터베이스를 사용하지 않습니다.

## MySQL 통합 테스트

통합 테스트 클래스에는 프로젝트의 `@IntegrationTest`를 사용합니다. 이 애노테이션은 Spring Boot 컨텍스트, `integration` 태그, 공통 MySQL Testcontainers 설정을 함께 적용합니다.

```java
@IntegrationTest
class ProductRepositoryIntegrationTest {
}
```

MySQL 컨테이너는 `MySqlTestcontainersConfiguration`에서 Spring 빈으로 관리합니다. `@ServiceConnection`이 컨테이너 접속 정보를 애플리케이션의 `DataSource`에 자동 적용하므로 테스트에 포트나 JDBC URL을 직접 작성하지 않습니다.

컨테이너 이미지는 로컬 개발 및 CI와 같은 `mysql:8.4`를 사용합니다. 테스트 실행 시 Flyway 마이그레이션이 빈 데이터베이스에 처음부터 적용됩니다.

현재 Walking Skeleton은 다음 흐름을 검증합니다.

1. MySQL 8.4 컨테이너를 자동으로 시작합니다.
2. Spring Boot가 컨테이너에 연결하고 Flyway V1을 적용합니다.
3. `products`, `product_quotas` 테이블 생성을 확인합니다.
4. 상품 데이터를 트랜잭션 안에서 저장합니다.
5. 롤백 후 데이터가 남지 않았는지 확인합니다.

## 실행 명령

순수 단위·웹 테스트만 실행할 때는 Docker가 필요하지 않습니다.

```powershell
.\gradlew.bat test
```

MySQL 통합 테스트는 Docker Desktop을 실행한 상태에서 수행합니다. MySQL 컨테이너는 테스트가 자동으로 관리하므로 `docker compose up`을 먼저 실행하지 않습니다.

```powershell
.\gradlew.bat integrationTest
```

일반 테스트와 통합 테스트를 모두 포함한 검증은 다음 명령을 사용합니다.

```powershell
.\gradlew.bat clean build
```

동시성 또는 부하 테스트는 명시적으로 선택해서 실행합니다.

```powershell
.\gradlew.bat concurrencyTest
.\gradlew.bat loadTest
```

macOS와 Linux에서는 `./gradlew`를 사용합니다.

## CI 기준

Pull Request와 `main` push에서는 `./gradlew build`를 실행합니다. `check`가 `integrationTest`에 의존하므로 단위 테스트와 MySQL 통합 테스트가 모두 통과해야 빌드가 성공합니다.

GitHub Actions 실행 환경에서는 Testcontainers가 MySQL을 직접 생성합니다. 워크플로에 별도의 MySQL service와 고정 접속 정보를 중복으로 설정하지 않습니다.

동시성·부하 테스트는 실행 시간과 결과 변동성이 크므로 기본 CI에 포함하지 않습니다. 해당 테스트가 추가될 때 수동 실행 또는 일정 기반의 별도 CI 잡으로 구성합니다.
