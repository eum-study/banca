# API 응답 및 예외 처리 규약

## 공통 응답 구조

본문이 있는 API 응답은 성공과 실패 모두 다음 필드를 사용합니다.

| 필드 | 타입 | 규칙 |
| --- | --- | --- |
| `status` | number | 실제 HTTP 응답 상태 코드와 같은 숫자를 사용합니다. |
| `code` | string | 클라이언트가 분기할 수 있는 안정적인 코드입니다. 대문자 스네이크 케이스로 작성합니다. |
| `message` | string | 사용자가 이해할 수 있는 설명입니다. 내부 구현 정보는 포함하지 않습니다. |
| `data` | object, array, primitive 또는 null | 성공 데이터나 Validation 오류 상세를 담고, 전달할 내용이 없으면 `null`을 사용합니다. |

성공 응답 예시:

```json
{
  "status": 200,
  "code": "SUCCESS",
  "message": "요청이 성공했습니다.",
  "data": {
    "productId": 1
  }
}
```

`204 No Content`는 HTTP 명세에 맞게 응답 본문을 보내지 않으며 공통 구조로 감싸지 않습니다.

## 에러 코드 규칙

- 에러 코드는 `INVALID_INPUT`, `PRODUCT_NOT_FOUND`처럼 대문자 스네이크 케이스로 작성합니다.
- 이미 배포한 에러 코드의 의미를 바꾸거나 다른 상황에 재사용하지 않습니다.
- 공통 오류는 `CommonErrorCode`, 기능별 오류는 각 기능 패키지의 `ErrorCode` 구현 enum에서 관리합니다.
- 응답의 `status`는 에러 코드에 정의한 HTTP 상태와 반드시 같아야 합니다.

HTTP 상태 매핑 기준:

| 상태 | 사용하는 경우 | 코드 예시 |
| --- | --- | --- |
| `400 Bad Request` | 형식, 필수값, 범위 등 요청 값이 잘못됨 | `INVALID_INPUT` |
| `401 Unauthorized` | 인증 정보가 없거나 유효하지 않음 | `AUTHENTICATION_REQUIRED` |
| `403 Forbidden` | 인증됐지만 해당 작업 권한이 없음 | `ACCESS_DENIED` |
| `404 Not Found` | 식별자에 해당하는 리소스가 없음 | `PRODUCT_NOT_FOUND` |
| `409 Conflict` | 중복이나 현재 상태 때문에 요청이 충돌함 | `PRODUCT_CODE_DUPLICATED` |
| `500 Internal Server Error` | 분류하지 못한 예상 밖의 서버 오류 | `INTERNAL_SERVER_ERROR` |

비즈니스 규칙 위반은 기능별 에러 코드와 함께 `BusinessException`을 사용합니다. 단순히 구현이 편하다는 이유로 모든 오류를 `400`으로 처리하지 않습니다.

## Validation 오류

Bean Validation 실패는 `400 / INVALID_INPUT`으로 응답합니다. 필드별 오류는 `data.fieldErrors`에 담습니다.

```json
{
  "status": 400,
  "code": "INVALID_INPUT",
  "message": "요청 값이 올바르지 않습니다.",
  "data": {
    "fieldErrors": [
      {
        "field": "name",
        "message": "상품명은 필수입니다."
      }
    ]
  }
}
```

## 예상하지 못한 오류

처리하지 않은 예외는 `500 / INTERNAL_SERVER_ERROR`로 통일합니다. SQL, 스택 트레이스, 클래스명, 내부 예외 메시지는 응답에 노출하지 않고 서버 로그에만 기록합니다.

```json
{
  "status": 500,
  "code": "INTERNAL_SERVER_ERROR",
  "message": "서버 내부 오류가 발생했습니다.",
  "data": null
}
```
