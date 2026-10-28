package com.ecommerce.orders_service.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponse> buildResponse(
            Exception ex,
            HttpStatus status,
            String code,
            HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                status.value(),
                code,
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(CartEmptyException.class)
    public ResponseEntity<ErrorResponse> handleCartEmpty(CartEmptyException ex, HttpServletRequest request) {
        return buildResponse(ex, HttpStatus.BAD_REQUEST, "CART_EMPTY", request);
    }

    @ExceptionHandler(ProductOutOfStockException.class)
    public ResponseEntity<ErrorResponse> handleOutOfStock(ProductOutOfStockException ex, HttpServletRequest request) {
        return buildResponse(ex, HttpStatus.CONFLICT, "PRODUCT_OUT_OF_STOCK", request);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex, HttpServletRequest request) {
        return buildResponse(ex, HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", request);
    }

    @ExceptionHandler(UnauthorizedOrderAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedOrderAccessException ex,
                                                            HttpServletRequest request) {
        return buildResponse(ex, HttpStatus.FORBIDDEN, "UNAUTHORIZER_ORDER_ACCESS", request);
    }

    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCartNotFound(CartNotFoundException ex, HttpServletRequest request) {
        return buildResponse(ex, HttpStatus.NOT_FOUND, "CART_NOT_FOUND", request);
    }

    @ExceptionHandler(InvalidCartOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCart(InvalidCartOperationException ex,
                                                           HttpServletRequest request) {
        return buildResponse(ex, HttpStatus.BAD_REQUEST, "INVALID_CART_OPERATION", request);
    }

    @ExceptionHandler(InvalidOrderStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrderState(InvalidOrderStateException ex,
                                                                 HttpServletRequest request) {
        return buildResponse(ex, HttpStatus.CONFLICT, "INVALID_ORDER_STATE", request);
    }

    @ExceptionHandler(UnauthorizedUserException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedUser(UnauthorizedUserException ex,
                                                                HttpServletRequest request) {
        return buildResponse(ex, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED_USER_EXCEPTION", request);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(ProductNotFoundException ex,
                                                               HttpServletRequest request) {
        return buildResponse(ex, HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", request);
    }

    @ExceptionHandler(BusinessOperationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessOperationException ex,
                                                                 HttpServletRequest request) {
        return buildResponse(ex, HttpStatus.BAD_REQUEST, "BUSINESS_OPERATION_ERROR", request);
    }

    @ExceptionHandler(CouponNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCouponNotFound(CouponNotFoundException ex,
                                                              HttpServletRequest request) {
        return buildResponse(ex, HttpStatus.NOT_FOUND, "COUPON_NOT_FOUND", request);
    }

    @ExceptionHandler(InvalidCouponException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCoupon(
            InvalidCouponException ex,
            HttpServletRequest request) {
        return buildResponse(ex, HttpStatus.BAD_REQUEST, "INVALID_COUPON", request);
    }

    @ExceptionHandler(InvalidPaymentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPayment(
            InvalidPaymentException ex, HttpServletRequest request) {
        return buildResponse(ex, HttpStatus.BAD_REQUEST, "INVALID_PAYMENT", request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        return buildResponse(ex, HttpStatus.BAD_REQUEST, "ILLEGAL_ARGUMENT", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));

        ErrorResponse error = new ErrorResponse(
                400,
                "VALIDATION_ERROR",
                message,
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

}
