package roomescape.global.exception;

import auth.exception.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.lang.reflect.Executable;
import java.util.stream.Collectors;

@ControllerAdvice
public class ExceptionController {

    private final Logger log = LoggerFactory.getLogger(ExceptionController.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Executable executable = e.getParameter().getExecutable();
        String where = executable.getDeclaringClass().getSimpleName() + "." + executable.getName();

        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> "%s = %s' (%s)".formatted(fe.getField(), fe.getRejectedValue(), fe.getDefaultMessage()))
                .collect(Collectors.joining(","));

        log.info("[{}] {}", where, detail);

        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.info("[{} {}] 요청 바디를 읽을 수 없습니다. {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Void> handleConstraintViolationException(ConstraintViolationException e, HandlerMethod handlerMethod) {
        String where = handlerMethod.getBeanType().getSimpleName() + "." + handlerMethod.getMethod().getName();

        String detail = e.getConstraintViolations().stream()
                .map(cv -> "%s = '%s' (%s)".formatted(cv.getPropertyPath(), cv.getInvalidValue(), cv.getMessage()))
                .collect(Collectors.joining(","));

        log.info("[{}] {}", where, detail);

        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Void> handleBindException(BindException e, HandlerMethod handlerMethod) {
        String where = handlerMethod.getBeanType().getSimpleName() + "." + handlerMethod.getMethod().getName();

        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> "%s = '%s' (%s)".formatted(fe.getField(), fe.getRejectedValue(), fe.getDefaultMessage()))
                .collect(Collectors.joining(","));

        log.info("[{}] {}", where, detail);

        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HandlerMethod handlerMethod) {
        String where = handlerMethod.getBeanType().getSimpleName() + "." + handlerMethod.getMethod().getName();

        String requiredType = e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown";
        String detail = "%s = '%s' (%s 타입으로 변환할 수 없습니다.)".formatted(e.getName(), e.getValue(), requiredType);

        log.info("[{}] {}", where, detail);

        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e, HandlerMethod handlerMethod) {
        String where = handlerMethod.getBeanType().getSimpleName() + "." + handlerMethod.getMethod().getName();

        String detail = "%s (%s) 필수 파라미터가 없습니다.".formatted(e.getParameterName(), e.getParameterType());

        log.info("[{}] {}", where, detail);

        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(RoomescapeException.class)
    public ResponseEntity<Void> handleRoomescapeException(RoomescapeException e, HandlerMethod handlerMethod) {
        String where = handlerMethod.getBeanType().getSimpleName() + "." + handlerMethod.getMethod().getName();
        log.info("[{}] {}",  where, e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).build();
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Void> handleAuthException(AuthException e, HandlerMethod handlerMethod) {
        String where = handlerMethod.getBeanType().getSimpleName() + "." + handlerMethod.getMethod().getName();
        log.info("[{}] {}",  where, e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).build();
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.info("[{} {}] 지원하지 않는 HTTP 메서드입니다. 지원하는 메서드: {}", request.getMethod(), request.getRequestURI(), e.getSupportedHttpMethods());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Void> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        log.info("[{} {}] 요청을 처리할 핸들러가 없습니다.", request.getMethod(), request.getRequestURI());
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("[{} {}] 처리되지 않은 예외", request.getMethod(), request.getRequestURI(), e);
        return ResponseEntity.internalServerError().build();
    }
}
