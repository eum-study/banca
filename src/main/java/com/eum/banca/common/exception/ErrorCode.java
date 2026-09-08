package com.eum.banca.common.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

	HttpStatus httpStatus();

	String code();

	String message();
}
