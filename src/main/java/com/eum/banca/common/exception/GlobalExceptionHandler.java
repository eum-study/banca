package com.eum.banca.common.exception;

import java.util.Comparator;
import java.util.List;

import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.eum.banca.common.web.ApiResponse;
import com.eum.banca.common.web.ValidationErrorData;
import com.eum.banca.common.web.ValidationErrorData.FieldErrorDetail;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
		ErrorCode errorCode = exception.getErrorCode();
		return ResponseEntity
			.status(errorCode.httpStatus())
			.body(ApiResponse.failure(errorCode, null));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<ValidationErrorData>> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException exception
	) {
		List<FieldErrorDetail> fieldErrors = exception.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(error -> new FieldErrorDetail(error.getField(), error.getDefaultMessage()))
			.sorted(Comparator.comparing(FieldErrorDetail::field))
			.toList();

		return invalidInput(new ValidationErrorData(fieldErrors));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiResponse<ValidationErrorData>> handleConstraintViolationException(
		ConstraintViolationException exception
	) {
		List<FieldErrorDetail> fieldErrors = exception.getConstraintViolations()
			.stream()
			.map(violation -> new FieldErrorDetail(
				violation.getPropertyPath().toString(),
				violation.getMessage()
			))
			.sorted(Comparator.comparing(FieldErrorDetail::field))
			.toList();

		return invalidInput(new ValidationErrorData(fieldErrors));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
		log.error("Unexpected exception occurred", exception);
		ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
		return ResponseEntity
			.status(errorCode.httpStatus())
			.body(ApiResponse.failure(errorCode, null));
	}

	private ResponseEntity<ApiResponse<ValidationErrorData>> invalidInput(ValidationErrorData data) {
		ErrorCode errorCode = CommonErrorCode.INVALID_INPUT;
		return ResponseEntity
			.status(errorCode.httpStatus())
			.body(ApiResponse.failure(errorCode, data));
	}
}
