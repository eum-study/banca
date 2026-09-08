package com.eum.banca.product.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import com.eum.banca.support.IntegrationTest;

@IntegrationTest
class ProductPersistenceIntegrationTest {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Test
	void flywayCreatesProductTables() {
		Integer productsTableCount = jdbcTemplate.queryForObject(
			"SELECT COUNT(*) FROM information_schema.tables "
				+ "WHERE table_schema = DATABASE() AND table_name IN ('products', 'product_quotas')",
			Integer.class
		);

		assertThat(productsTableCount).isEqualTo(2);
	}

	@Test
	void rollsBackProductWriteWithinTransaction() {
		String productCode = "TEST-" + UUID.randomUUID().toString().substring(0, 8);
		LocalDateTime now = LocalDateTime.of(2026, 9, 8, 0, 0);

		transactionTemplate.executeWithoutResult(status -> {
			int insertedRows = jdbcTemplate.update(
				"""
					INSERT INTO products (
					    product_code, name, sale_status, sale_start_at, sale_end_at, created_at, updated_at
					) VALUES (?, ?, ?, ?, ?, ?, ?)
					""",
				productCode,
				"통합 테스트 상품",
				"ON_SALE",
				Timestamp.valueOf(now),
				Timestamp.valueOf(now.plusDays(1)),
				Timestamp.valueOf(now),
				Timestamp.valueOf(now)
			);

			assertThat(insertedRows).isOne();
			assertThat(countProductsByCode(productCode)).isOne();
			status.setRollbackOnly();
		});

		assertThat(countProductsByCode(productCode)).isZero();
	}

	private int countProductsByCode(String productCode) {
		Integer count = jdbcTemplate.queryForObject(
			"SELECT COUNT(*) FROM products WHERE product_code = ?",
			Integer.class,
			productCode
		);
		return count == null ? 0 : count;
	}
}
