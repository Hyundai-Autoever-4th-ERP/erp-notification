package erp.notification.service;

import erp.notification.domain.EventType;
import erp.notification.dto.EventReceipt;
import erp.notification.dto.NotificationEvent;
import erp.notification.dto.NotificationPage;
import erp.notification.dto.NotificationResponse;
import erp.notification.exception.NotificationError;
import erp.notification.repository.NotificationRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public EventReceipt accept(NotificationEvent event) {
        LocalDateTime time = event.occurredAt().withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
        if (time.getNano() % 1000 != 0) {
            throw NotificationError.invalid();
        }
        var id = notificationRepository.insert(event, time);
        if (id.isPresent()) {
            notificationRepository.deliverInApp(id.get());
        } else if (!notificationRepository.matches(event, time)) {
            throw NotificationError.conflict();
        }
        return new EventReceipt(event.eventId(), true);
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public NotificationPage list(String employeeNumber, EventType type, Boolean read, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw NotificationError.invalid();
        }
        return notificationRepository.find(employeeNumber, type, read, page, size);
    }

    public NotificationResponse markRead(long id, String employeeNumber) {
        return notificationRepository.markRead(id, employeeNumber).orElseThrow(NotificationError::missing);
    }
}
