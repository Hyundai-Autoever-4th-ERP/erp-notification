package erp.notification;

import erp.notification.domain.EventType;
import erp.notification.dto.NotificationEvent;
import erp.notification.exception.NotificationError;
import erp.notification.service.NotificationService;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class NotificationServiceTest {
    @Autowired NotificationService notificationService;
    @Autowired JdbcTemplate jdbc;

    private final String eventId = UUID.randomUUID().toString();
    private final String recipient = UUID.randomUUID().toString();

    @Test
    void retryKeepsOneNotificationAndOneDelivery() {
        var original = event("SO-1");
        assertThat(notificationService.accept(original)).isEqualTo(notificationService.accept(original));
        var result = notificationService.list(recipient, null, false, 0, 20);
        assertThat(result.totalElements()).isEqualTo(1);
        long id = result.content().getFirst().id();
        assertThat(jdbc.queryForObject("select count(*) from notification_deliveries where notification_id = ?",
                Long.class, id)).isEqualTo(1);
        assertThatThrownBy(() -> notificationService.accept(event("SO-2")))
                .isInstanceOf(NotificationError.class);
    }

    @Test
    void readIsPrivateAndPreservesFirstTimestamp() {
        notificationService.accept(event("SO-1"));
        long id = notificationService.list(recipient, null, false, 0, 20).content().getFirst().id();
        var read = notificationService.markRead(id, recipient);
        assertThat(read.readAt()).isNotNull().isEqualTo(notificationService.markRead(id, recipient).readAt());
        assertThat(notificationService.list(recipient, null, false, 0, 20).content()).isEmpty();
        assertThatThrownBy(() -> notificationService.markRead(id, "another-employee"))
                .isInstanceOf(NotificationError.class);
    }

    private NotificationEvent event(String sourceId) {
        return new NotificationEvent(eventId, EventType.SO_APPROVED, recipient, "SO", sourceId,
                OffsetDateTime.parse("2026-11-27T09:00:00+09:00"));
    }
}
