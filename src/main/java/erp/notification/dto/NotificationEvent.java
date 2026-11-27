package erp.notification.dto;

import erp.notification.domain.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record NotificationEvent(
        @NotBlank @Size(max = 100) String eventId,
        @NotNull EventType type,
        @NotBlank @Size(max = 40) String recipientEmployeeNumber,
        @NotBlank @Size(max = 40) String sourceType,
        @NotBlank @Size(max = 100) String sourceId,
        @NotNull OffsetDateTime occurredAt
) {}
