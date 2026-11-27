package erp.notification.repository;

import erp.notification.domain.EventType;
import erp.notification.dto.NotificationEvent;
import erp.notification.dto.NotificationPage;
import erp.notification.dto.NotificationResponse;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationRepository {
    private final JdbcClient jdbc;

    public NotificationRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Long> insert(NotificationEvent event, LocalDateTime occurredAt) {
        return jdbc.sql("""
                INSERT INTO notifications (event_id, recipient_employee_number, event_type,
                    summary, body, source_type, source_reference, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (event_id, recipient_employee_number) DO NOTHING
                RETURNING id
                """)
                .params(event.eventId(), event.recipientEmployeeNumber(), event.type().name(),
                        event.type().summary(), event.sourceId() + " · " + event.type().summary(),
                        event.sourceType(), event.sourceId(), occurredAt)
                .query(Long.class).optional();
    }

    public boolean matches(NotificationEvent event, LocalDateTime occurredAt) {
        return jdbc.sql("""
                SELECT EXISTS (SELECT 1 FROM notifications
                    WHERE event_id = ? AND recipient_employee_number = ? AND event_type = ?
                      AND source_type = ? AND source_reference = ? AND created_at = ?)
                """)
                .params(event.eventId(), event.recipientEmployeeNumber(), event.type().name(),
                        event.sourceType(), event.sourceId(), occurredAt)
                .query(Boolean.class).single();
    }

    public void deliverInApp(long id) {
        jdbc.sql("""
                INSERT INTO notification_deliveries (notification_id, channel, delivery_status, sent_at)
                VALUES (?, 'INAPP', 'SENT', CURRENT_TIMESTAMP AT TIME ZONE 'UTC')
                """).param(id).update();
    }

    public Optional<NotificationResponse> markRead(long id, String employeeNumber) {
        return jdbc.sql("""
                UPDATE notifications SET read_at = COALESCE(read_at, CURRENT_TIMESTAMP AT TIME ZONE 'UTC')
                WHERE id = ? AND recipient_employee_number = ?
                RETURNING *
                """).params(id, employeeNumber).query(this::map).optional();
    }

    public NotificationPage find(String employeeNumber, EventType type, Boolean read, int page, int size) {
        StringBuilder where = new StringBuilder(" FROM notifications WHERE true");
        List<Object> params = new ArrayList<>();
        if (employeeNumber != null) {
            where.append(" AND recipient_employee_number = ?");
            params.add(employeeNumber);
        }
        if (type != null) {
            where.append(" AND event_type = ?");
            params.add(type.name());
        }
        if (read != null) {
            where.append(read ? " AND read_at IS NOT NULL" : " AND read_at IS NULL");
        }
        long total = jdbc.sql("SELECT COUNT(*)" + where).params(params).query(Long.class).single();
        params.add(size);
        params.add((long) page * size);
        var content = jdbc.sql("SELECT *" + where + " ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?")
                .params(params).query(this::map).list();
        return new NotificationPage(content, page, size, total, Math.ceilDiv(total, size));
    }

    private NotificationResponse map(ResultSet rs, int row) throws SQLException {
        return new NotificationResponse(rs.getLong("id"), rs.getString("recipient_employee_number"),
                rs.getString("event_type"), rs.getString("summary"), rs.getString("body"),
                rs.getString("source_type"), rs.getString("source_reference"), "INAPP",
                utc(rs, "created_at"), utc(rs, "read_at"));
    }

    private OffsetDateTime utc(ResultSet rs, String column) throws SQLException {
        LocalDateTime time = rs.getObject(column, LocalDateTime.class);
        return time == null ? null : time.atOffset(ZoneOffset.UTC);
    }
}
