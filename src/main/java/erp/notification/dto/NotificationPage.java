package erp.notification.dto;

import java.util.List;

public record NotificationPage(List<NotificationResponse> content, int page, int size,
                               long totalElements, long totalPages) {}
