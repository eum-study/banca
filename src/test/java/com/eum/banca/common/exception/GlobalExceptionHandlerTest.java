package com.eum.banca.common.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eum.banca.common.web.ApiResponse;

class GlobalExceptionHandlerTest {

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();

		mockMvc = MockMvcBuilders
			.standaloneSetup(new TestController())
			.setControllerAdvice(new GlobalExceptionHandler())
			.setValidator(validator)
			.build();
	}

	@Test
	void returnsFieldErrorsWhenRequestBodyValidationFails() throws Exception {
		mockMvc.perform(post("/test/validation")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.code").value("INVALID_INPUT"))
			.andExpect(jsonPath("$.message").value("요청 값이 올바르지 않습니다."))
			.andExpect(jsonPath("$.data.fieldErrors[0].field").value("name"))
			.andExpect(jsonPath("$.data.fieldErrors[0].message").value("상품명은 필수입니다."));
	}

	@Test
	void mapsBusinessExceptionToItsHttpStatusAndCode() throws Exception {
		mockMvc.perform(get("/test/conflict"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.status").value(409))
			.andExpect(jsonPath("$.code").value("TEST_CONFLICT"))
			.andExpect(jsonPath("$.message").value("요청이 현재 상태와 충돌합니다."))
			.andExpect(jsonPath("$.data").isEmpty());
	}

	@Test
	void hidesInternalDetailsWhenUnexpectedExceptionOccurs() throws Exception {
		mockMvc.perform(get("/test/unexpected"))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.status").value(500))
			.andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
			.andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."))
			.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(
				org.hamcrest.Matchers.containsString("sensitive database detail")
			)))
			.andExpect(jsonPath("$.data").isEmpty());
	}

	@RestController
	@RequestMapping("/test")
	private static class TestController {

		@PostMapping("/validation")
		ApiResponse<TestRequest> validate(@Valid @RequestBody TestRequest request) {
			return ApiResponse.success(request);
		}

		@GetMapping("/conflict")
		ApiResponse<Void> conflict() {
			throw new BusinessException(TestErrorCode.TEST_CONFLICT);
		}

		@GetMapping("/unexpected")
		ApiResponse<Void> unexpected() {
			throw new IllegalStateException("sensitive database detail");
		}
	}

	private record TestRequest(@NotBlank(message = "상품명은 필수입니다.") String name) {
	}

	private enum TestErrorCode implements ErrorCode {

		TEST_CONFLICT;

		@Override
		public HttpStatus httpStatus() {
			return HttpStatus.CONFLICT;
		}

		@Override
		public String code() {
			return name();
		}

		@Override
		public String message() {
			return "요청이 현재 상태와 충돌합니다.";
		}
	}
}
