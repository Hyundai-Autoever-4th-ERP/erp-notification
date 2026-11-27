# ERP Notification

## 로컬 실행

JDK 25와 Docker가 필요합니다. 클론한 저장소에서 커밋 훅을 한 번 설정하세요.

```sh
git config --local core.hooksPath .githooks
```

로컬 DB를 켜고 앱을 실행하세요.

```sh
docker compose up -d
./gradlew bootRun
```

IntelliJ에서는 프로젝트 SDK와 Gradle JVM을 25로 설정하고 `NotificationApplication`을 실행합니다. Windows 터미널에서는 `.\gradlew.bat bootRun`을 사용합니다.

- 상태 확인 [Actuator Health](http://localhost:8086/actuator/health)
- API 문서 [Swagger UI](http://localhost:8086/swagger-ui/index.html)
- 테스트는 로컬 DB를 실행한 상태에서 `./gradlew test`로 확인합니다.

## 로컬 DB 접속

| 접속 항목 | 값 |
| --- | --- |
| 주소 | `localhost:15436` |
| 데이터베이스 | `noti_db` |
| 사용자 | `noti_app` |
| 로컬 비밀번호 | `noti_local` |

`docker compose down`으로 종료해도 데이터는 볼륨에 남습니다.

## Swagger에서 알림 확인

로컬 테스트는 Node.js로 임시 서명 키를 생성한다. 첫 번째 터미널에서 아래 명령을 실행한 채 두고 두 번째 터미널에서 앱을 시작한다.

```sh
node scripts/local-swagger-auth.mjs
```

```sh
AUTH_JWKS_URI=http://127.0.0.1:18086/internal/auth/jwks \
NOTIFICATION_PUBLISHERS=local-sales SERVER_ADDRESS=127.0.0.1 ./gradlew bootRun
```

Swagger의 **Authorize**에 역할별 토큰 문자열만 입력한다. 역할을 변경할 때는 Logout 후 다시 입력한다.

1. **NOTIFICATION_PUBLISHER**로 아래 이벤트를 접수하면 202를 받는다. 같은 요청을 재전송하면 기존 결과를 반환하고, 같은 수신자·eventId로 sourceId를 바꾸면 409를 받는다.
2. **BRANCH_STAFF**로 내 목록을 조회하고 응답의 id로 읽음 처리한다. 목록 조회는 200, 읽음 재호출은 처음과 같은 readAt을 반환한다. 이 토큰으로 관리자 목록을 열면 403이다.
3. **ADMIN**으로 전체 목록을 조회할 수 있다. 다른 직원의 알림을 읽음 처리하면 404를 반환한다.

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

현재 인앱 채널을 제공한다. 사용자 JWT의 sub는 사번, role은 ERP 역할로 해석한다. 서비스 간 인증은 Auth 담당자와 협의 중이다. 이 구현에서 발행하려면 서명이 유효하고 role이 NOTIFICATION_PUBLISHER이며 sub가 NOTIFICATION_PUBLISHERS 목록에 있어야 한다. 허용 목록의 기본값은 비어 있다. 로컬 테스트용 키는 프로세스를 재시작하면 교체되고 토큰은 1시간 뒤 만료된다.

occurredAt은 시간대가 있는 ISO 8601 값으로 소수점 이하 6자리까지 지원한다. 이벤트 시각과 읽음 시각은 UTC 기준으로 저장한다. 목록의 page는 0부터 시작하며 size는 1~100, 기본값은 20이다.
