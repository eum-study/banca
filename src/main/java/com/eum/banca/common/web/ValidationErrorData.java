package com.eum.banca.common.web;

import java.util.List;

public record ValidationErrorData(List<FieldErrorDetail> fieldErrors) {

	public ValidationErrorData {
		fieldErrors = List.copyOf(fieldErrors);
	}

	public record FieldErrorDetail(String field, String message) {
	}
}
