package erp.notification.dto;

import java.time.OffsetDateTime;

public record NotificationResponse(
        long id, String recipientEmployeeNumber, String type, String summary, String body,
        String sourceType, String sourceId, String channel, OffsetDateTime createdAt, OffsetDateTime readAt
) {}
