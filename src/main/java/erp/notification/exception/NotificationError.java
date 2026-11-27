package erp.notification.exception;

import org.springframework.http.HttpStatus;

public class NotificationError extends RuntimeException {
    final HttpStatus status;
    final String code;

    private NotificationError(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static NotificationError invalid() {
        return new NotificationError(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "요청값을 확인해 주세요.");
    }

    public static NotificationError unauthorized() {
        return new NotificationError(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "토큰에 사번이 없습니다.");
    }

    public static NotificationError missing() {
        return new NotificationError(HttpStatus.NOT_FOUND, "NOTIFICATION_NOT_FOUND", "알림을 찾을 수 없습니다.");
    }

    public static NotificationError conflict() {
        return new NotificationError(HttpStatus.CONFLICT, "EVENT_CONFLICT", "같은 이벤트 키로 다른 내용이 접수됐습니다.");
    }
}
