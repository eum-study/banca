# banca

은행 앱의 비대면 보험 가입 흐름을 다루는 방카슈랑스 백엔드 프로젝트입니다.

현재 단계는 M0이며, `product`와 `common`만으로 최소 프로젝트 골격을 구성하고 있습니다.

## 기술 스택

- Java 21
- Spring Boot 3.5
- Spring MVC, Spring Data JPA, Bean Validation
- MySQL 8.4, Flyway
- Gradle Wrapper

## 로컬 실행

필요한 도구:

- JDK 21
- Docker Desktop과 Docker Compose

MySQL을 실행합니다.

```shell
docker compose up -d --wait mysql
```

Windows에서 빌드와 테스트를 실행합니다.

```powershell
.\gradlew.bat clean build
```

애플리케이션을 실행합니다.

```powershell
.\gradlew.bat bootRun
```

macOS 또는 Linux에서는 `./gradlew`를 사용합니다.

기본 데이터베이스 접속 정보:

| 항목 | 값 |
| --- | --- |
| 주소 | `localhost:13306` |
| 데이터베이스 | `bancassurance` |
| 사용자 | `banca` |
| 비밀번호 | `banca` |

다른 환경에서는 다음 환경변수로 접속 정보를 덮어쓸 수 있습니다.

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

## 시간 기준

DB 연결과 Hibernate의 저장 기준 시간대는 UTC로 통일합니다. 한국 영업일처럼 도메인 시간대가 필요한 계산은 애플리케이션 코드에서 `Asia/Seoul`을 명시합니다.

## 데이터베이스 마이그레이션

Flyway 스크립트는 `src/main/resources/db/migration`에 둡니다. 공유되거나 `main`에 병합된 마이그레이션은 수정하지 않고 다음 버전의 스크립트를 추가합니다.

## 코드 구조와 협업 규칙

패키지 의존 방향, 네이밍, 브랜치와 커밋 규칙은 [CONTRIBUTING.md](CONTRIBUTING.md)를 따릅니다.

API 공통 응답 구조와 예외 처리 기준은 [API 응답 및 예외 처리 규약](docs/api-conventions.md)을 따릅니다.

Jira 이슈가 `PLAN READY`로 전환될 때 GitHub 이슈와 브랜치를 만드는 연동 설정은 [Jira-GitHub 자동화 가이드](docs/jira-github-automation.md)를 따릅니다.
