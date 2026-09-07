# Jira-GitHub 자동화

Jira 이슈의 작업 정보를 모두 작성한 뒤 상태를 `PLAN READY`로 변경하면 GitHub 이슈와 브랜치를 자동 생성한다.

```text
Jira: PLAN READY 전환
  -> GitHub repository_dispatch
  -> GitHub Issue 생성
  -> 기본 브랜치에서 작업 브랜치 생성
```

## 사전 조건

1. `.github/workflows/create-from-jira.yml`이 GitHub 저장소의 기본 브랜치에 있어야 한다.
2. Jira Automation에서 사용할 GitHub fine-grained personal access token을 준비한다.
3. 토큰의 저장소 범위는 `eum-study/banca` 하나로 제한한다.
4. 토큰에는 `Contents: Read and write` 권한만 부여한다. GitHub 이슈와 브랜치는 workflow의 `GITHUB_TOKEN`이 생성한다.
5. 토큰은 Jira의 `Send web request` Authorization 헤더에 숨김 값으로 저장하고 문서나 저장소에 기록하지 않는다.

## Jira Automation 규칙

### 1. Trigger

- Trigger: `Work item transitioned`
- To status: `PLAN READY`

### 2. Conditions

아래 조건을 모두 만족할 때만 웹 요청을 전송한다.

- Project key equals `BNC`
- Summary is not empty
- Description is not empty
- 프로젝트에서 추가로 필수로 정한 계획 필드가 모두 채워짐

필수 필드는 가능하면 Jira workflow의 `PLAN READY` 전환 validator에도 등록한다. 그러면 정보가 빠진 이슈는 상태 전환 자체가 되지 않는다.

### 3. Send web request

- URL: `https://api.github.com/repos/eum-study/banca/dispatches`
- Method: `POST`
- Web request body: `Custom data`

Headers:

| 이름 | 값 |
| --- | --- |
| `Accept` | `application/vnd.github+json` |
| `Authorization` | `Bearer <GitHub token>` |
| `X-GitHub-Api-Version` | `2022-11-28` |
| `Content-Type` | `application/json` |

`Authorization` 값은 반드시 숨김 처리한다.

Request body:

```json
{
  "event_type": "jira-plan-ready",
  "client_payload": {
    "ticketKey": {{issue.key.asJsonString}},
    "summary": {{issue.summary.asJsonString}},
    "description": {{issue.description.asJsonString}},
    "jiraUrl": "{{baseUrl}}/browse/{{issue.key}}"
  }
}
```

GitHub이 `204 No Content`를 반환하면 dispatch 요청이 접수된 것이다. 실제 생성 성공 여부는 GitHub Actions의 `Create GitHub work from Jira` 실행 결과에서 확인한다.

## 생성 규칙

- GitHub 이슈 제목: `[BNC-5] 이슈 요약`
- 브랜치명: `BNC-5-이슈-요약`
- 브랜치는 저장소의 현재 기본 브랜치 HEAD에서 생성한다.
- Jira 요약이 변경되더라도 동일한 Jira 키 접두사의 기존 GitHub 이슈와 브랜치를 재사용한다.
- 동일한 Jira 키 접두사의 브랜치가 여러 개면 임의로 선택하지 않고 workflow를 실패시킨다.
- 이슈 본문에는 Jira 링크, 브랜치명, Jira 설명을 기록한다.

## 최초 검증 순서

1. workflow를 기본 브랜치에 병합한다.
2. 테스트용 Jira 이슈에 Summary와 Description을 입력한다.
3. 상태를 `PLAN READY`로 전환한다.
4. Jira Automation audit log에서 GitHub 응답이 `204`인지 확인한다.
5. GitHub Actions 실행이 성공했는지 확인한다.
6. GitHub 이슈와 브랜치가 각각 하나씩 생성됐는지 확인한다.
7. Jira 이슈를 다른 상태로 옮겼다가 다시 `PLAN READY`로 전환해 중복 생성되지 않는지 확인한다.

## 장애 확인

- GitHub Actions가 시작되지 않음: workflow가 기본 브랜치에 있는지, `event_type`이 `jira-plan-ready`인지 확인한다.
- GitHub 응답이 `401` 또는 `403`: Jira에 저장한 토큰과 `Contents` 권한을 확인한다.
- GitHub 응답이 `404`: 토큰의 저장소 범위와 저장소 주소를 확인한다.
- workflow가 입력값 오류로 실패: Jira 요청 JSON의 `ticketKey`, `summary`, `description`, `jiraUrl`을 확인한다.
