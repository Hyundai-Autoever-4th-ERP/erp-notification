package erp.notification.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
class NotificationAdvice {
    @ExceptionHandler(NotificationError.class)
    ResponseEntity<ApiError> domain(NotificationError error) {
        return ResponseEntity.status(error.status).body(new ApiError(error.code, error.getMessage()));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class})
    ResponseEntity<ApiError> invalid() {
        return domain(NotificationError.invalid());
    }
}
