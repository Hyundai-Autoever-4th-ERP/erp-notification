# 현대파츠 ERP · 알림

구매·판매·재고 서비스에서 받은 업무 이벤트를 직원별 인앱 알림으로 저장한다. 직원은 자신의 알림을 조회하고 읽음 처리할 수 있고, 관리자는 수신자와 이벤트 종류별로 전체 알림을 조회할 수 있다.

## API

| 메서드 | 경로 | 기능 | 권한 |
| --- | --- | --- | --- |
| GET | `/api/v1/notifications` | 내 알림 목록. `read`로 읽음 여부 필터 | 로그인 사용자 |
| PATCH | `/api/v1/notifications/{id}/read` | 최초 읽음 시각 기록. 다시 호출해도 같은 시각 반환 | 수신자 본인 |
| GET | `/api/v1/notifications/admin` | 사번·이벤트 종류·읽음 여부로 전체 목록 조회 | `ADMIN` |
| POST | `/internal/notifications/events` | 이벤트 접수. 성공 시 202 반환 | 허용된 발신 서비스 |

사용자 JWT의 `sub`를 수신자 사번으로 사용한다. 관리자도 타인의 알림을 읽음 처리하면 404를 받는다. 이벤트와 인앱 전달 기록은 한 트랜잭션에 저장한다. 같은 수신자에게 같은 `eventId`와 내용을 다시 보내면 기존 접수 결과를 반환하고, 내용이 달라지면 409를 반환한다.

## 로컬 실행과 Swagger

JDK 25, Docker, Node.js 18 이상을 사용한다. 첫 번째 터미널에서 PostgreSQL과 테스트 토큰 발급기를 실행한다.

```sh
docker compose up -d
node scripts/local-swagger-auth.mjs
```

두 번째 터미널에서 앱을 실행한다.

```sh
AUTH_JWKS_URI=http://127.0.0.1:18086/internal/auth/jwks \
NOTIFICATION_PUBLISHERS=local-sales SERVER_ADDRESS=127.0.0.1 ./gradlew bootRun
```

[로컬 Swagger](http://localhost:8086/swagger-ui/index.html)의 **Authorize**에 역할별 토큰을 입력해 아래 순서로 확인한다.

1. `NOTIFICATION_PUBLISHER`로 아래 이벤트를 접수한다. 같은 요청을 두 번 보내도 알림은 한 건이다.
2. `BRANCH_STAFF`로 바꿔 내 목록을 조회한다. 테스트 사번은 `DEMO-001`이다. 응답의 `id`로 읽음 처리하면 `readAt`에 최초 시각이 반환된다.

```json
{
  "eventId": "demo-so-001-approved",
  "type": "SO_APPROVED",
  "recipientEmployeeNumber": "DEMO-001",
  "sourceType": "SO",
  "sourceId": "SO-001",
  "occurredAt": "2026-11-27T09:00:00+09:00"
}
```

로컬 DB는 `localhost:15436/noti_db`를 사용한다. DB가 실행 중이면 `./gradlew test`로 검증할 수 있다. 내부 이벤트 접수는 발신 JWT의 역할이 `NOTIFICATION_PUBLISHER`이고 `sub`가 `NOTIFICATION_PUBLISHERS`에 포함될 때 허용한다. 기본 허용 목록은 비어 있으며, 실제 서비스 토큰 규격은 Auth 담당자와 협의 중이다. [인증 설정](src/main/java/erp/notification/config/NotificationSecurity.java)
