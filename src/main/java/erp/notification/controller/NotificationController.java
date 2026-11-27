package erp.notification.controller;

import erp.notification.controller.docs.NotificationApi;
import erp.notification.domain.EventType;
import erp.notification.dto.EventReceipt;
import erp.notification.dto.NotificationEvent;
import erp.notification.dto.NotificationPage;
import erp.notification.dto.NotificationResponse;
import erp.notification.exception.NotificationError;
import erp.notification.service.NotificationService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController implements NotificationApi {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public NotificationPage mine(Jwt jwt, Boolean read, int page, int size) {
        return notificationService.list(employeeNumber(jwt), null, read, page, size);
    }

    @Override
    public NotificationPage monitor(String recipientEmployeeNumber, EventType type, Boolean read,
                                    int page, int size) {
        return notificationService.list(recipientEmployeeNumber, type, read, page, size);
    }

    @Override
    public NotificationResponse markRead(long id, Jwt jwt) {
        return notificationService.markRead(id, employeeNumber(jwt));
    }

    @Override
    public EventReceipt accept(NotificationEvent event) {
        return notificationService.accept(event);
    }

    private String employeeNumber(Jwt jwt) {
        String employeeNumber = jwt.getSubject();
        if (employeeNumber == null || employeeNumber.isBlank()) {
            throw NotificationError.unauthorized();
        }
        return employeeNumber;
    }
}
