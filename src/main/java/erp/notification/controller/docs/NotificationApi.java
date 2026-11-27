package erp.notification.controller.docs;

import erp.notification.domain.EventType;
import erp.notification.dto.EventReceipt;
import erp.notification.dto.NotificationEvent;
import erp.notification.dto.NotificationPage;
import erp.notification.dto.NotificationResponse;
import erp.notification.exception.ApiError;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@Tag(name = "알림")
@SecurityRequirement(name = "bearerAuth")
public interface NotificationApi {
    @ApiResponse(responseCode = "200", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "400", description = "요청 값 오류",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/api/v1/notifications")
    @Operation(summary = "내 알림 목록", description = "최신순으로 조회합니다. read를 생략하면 읽음 여부와 관계없이 반환합니다.")
    NotificationPage mine(@AuthenticationPrincipal Jwt jwt, @RequestParam(required = false) Boolean read,
                                 @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size);

    @ApiResponse(responseCode = "200", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "400", description = "요청 값 오류",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "403", description = "권한 없음",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/api/v1/notifications/admin")
    @Operation(summary = "전체 알림 모니터링", description = "ADMIN 전용. 사번, 이벤트 종류, 읽음 여부로 검색합니다.")
    NotificationPage monitor(@RequestParam(required = false) String recipientEmployeeNumber,
                                    @RequestParam(required = false) EventType type,
                                    @RequestParam(required = false) Boolean read,
                                    @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size);

    @ApiResponse(responseCode = "200", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "401", description = "인증 실패",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "본인 알림을 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PatchMapping("/api/v1/notifications/{id}/read")
    @Operation(summary = "내 알림 읽음 처리", description = "반복 호출해도 처음 읽은 시각을 유지합니다.")
    NotificationResponse markRead(@PathVariable long id, @AuthenticationPrincipal Jwt jwt);

    @ApiResponse(responseCode = "400", description = "요청 값 오류",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "403", description = "권한 없음",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "같은 이벤트 키의 내용 불일치",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping("/internal/notifications/events")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "도메인 이벤트 접수", description = "발신 서비스 전용. 재시도 시 같은 eventId와 내용을 사용합니다.")
    @ApiResponse(responseCode = "202", description = "이벤트 접수", useReturnTypeSchema = true)
    EventReceipt accept(@Valid @RequestBody NotificationEvent event);
}
