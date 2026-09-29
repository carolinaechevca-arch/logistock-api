package com.logistock.inventory.configuration.exceptionhandler;

import com.logistock.inventory.domain.constants.InventoryConstants;
import com.logistock.inventory.domain.exception.InvalidProductException;
import com.logistock.inventory.domain.exception.InvalidInventoryMovementException;
import com.logistock.inventory.domain.exception.InvalidOrderException;
import com.logistock.inventory.domain.exception.InvalidOrderIdException;
import com.logistock.inventory.domain.exception.InvalidProductIdException;
import com.logistock.inventory.domain.exception.InvalidPaginationException;
import com.logistock.inventory.domain.exception.InvalidStockRangeException;
import com.logistock.inventory.domain.exception.InsufficientStockException;
import com.logistock.inventory.domain.exception.ProductNotFoundException;
import com.logistock.inventory.domain.exception.ProductHasStockException;
import com.logistock.inventory.domain.exception.OrderNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Clock clock;

    @ExceptionHandler(InvalidProductException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidProduct(
            InvalidProductException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getCode(),
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(InvalidInventoryMovementException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidInventoryMovement(
            InvalidInventoryMovementException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getCode(),
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(InvalidOrderException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidOrder(
            InvalidOrderException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getCode(),
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(InvalidOrderIdException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidOrderId(
            InvalidOrderIdException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getCode(),
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleOrderNotFound(
            OrderNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getCode(),
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiErrorResponse> handleInsufficientStock(
            InsufficientStockException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getCode(),
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiErrorResponse> handleConcurrentInventoryUpdate(
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                InventoryConstants.CONCURRENT_INVENTORY_UPDATE,
                InventoryConstants.CONCURRENT_INVENTORY_UPDATE_MESSAGE,
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(InvalidProductIdException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidProductId(
            InvalidProductIdException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getCode(),
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleProductNotFound(
            ProductNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getCode(),
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(InvalidStockRangeException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidStockRange(
            InvalidStockRangeException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getCode(),
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(InvalidPaginationException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidPagination(
            InvalidPaginationException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getCode(),
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(ProductHasStockException.class)
    public ResponseEntity<ApiErrorResponse> handleProductHasStock(
            ProductHasStockException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getCode(),
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> details = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                details.putIfAbsent(error.getField(), error.getDefaultMessage())
        );
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "The request contains invalid fields",
                request.getRequestURI(),
                details
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String code,
            String message,
            String path,
            Map<String, String> details
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(clock),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                path,
                details
        );
        return ResponseEntity.status(status).body(response);
    }
}
