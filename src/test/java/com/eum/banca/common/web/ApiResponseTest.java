package com.eum.banca.common.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ApiResponseTest {

	@Test
	void createsDefaultSuccessResponse() {
		ApiResponse<String> response = ApiResponse.success("result");

		assertThat(response.status()).isEqualTo(200);
		assertThat(response.code()).isEqualTo("SUCCESS");
		assertThat(response.message()).isEqualTo("요청이 성공했습니다.");
		assertThat(response.data()).isEqualTo("result");
	}

	@Test
	void rejectsCodeThatIsNotUppercaseSnakeCase() {
		assertThatThrownBy(() -> ApiResponse.success(HttpStatus.OK, "invalid-code", "성공", null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("code must be uppercase snake case");
	}

	@Test
	void rejectsNonSuccessStatusForSuccessResponse() {
		assertThatThrownBy(() -> ApiResponse.success(HttpStatus.BAD_REQUEST, "SUCCESS", "성공", null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("success status must be 2xx");
	}
}
