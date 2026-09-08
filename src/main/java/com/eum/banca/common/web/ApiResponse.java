package com.eum.banca.common.web;

import java.util.Objects;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;

import com.eum.banca.common.exception.ErrorCode;

public record ApiResponse<T>(
	int status,
	String code,
	String message,
	T data
) {

	private static final Pattern CODE_PATTERN = Pattern.compile("[A-Z][A-Z0-9]*(?:_[A-Z0-9]+)*");

	public ApiResponse {
		if (status < 100 || status > 599) {
			throw new IllegalArgumentException("status must be a valid HTTP status code");
		}
		if (!CODE_PATTERN.matcher(Objects.requireNonNull(code, "code must not be null")).matches()) {
			throw new IllegalArgumentException("code must be uppercase snake case");
		}
		if (Objects.requireNonNull(message, "message must not be null").isBlank()) {
			throw new IllegalArgumentException("message must not be blank");
		}
	}

	public static <T> ApiResponse<T> success(T data) {
		return success(HttpStatus.OK, "SUCCESS", "요청이 성공했습니다.", data);
	}

	public static ApiResponse<Void> success() {
		return success(null);
	}

	public static <T> ApiResponse<T> success(HttpStatus status, String code, String message, T data) {
		if (!status.is2xxSuccessful()) {
			throw new IllegalArgumentException("success status must be 2xx");
		}
		return new ApiResponse<>(status.value(), code, message, data);
	}

	public static <T> ApiResponse<T> failure(ErrorCode errorCode, T data) {
		Objects.requireNonNull(errorCode, "errorCode must not be null");
		return new ApiResponse<>(
			errorCode.httpStatus().value(),
			errorCode.code(),
			errorCode.message(),
			data
		);
	}
}
