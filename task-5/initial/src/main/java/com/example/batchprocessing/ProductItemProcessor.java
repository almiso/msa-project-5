package com.example.batchprocessing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProductItemProcessor implements ItemProcessor<Product, Product> {

	private static final Logger log = LoggerFactory.getLogger(ProductItemProcessor.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public Product process(final Product product) {
		String loyaltyStatus;
		try {
			loyaltyStatus = jdbcTemplate.queryForObject(
				"SELECT loyalityData FROM loyality_data WHERE productSku = ?",
				String.class,
				product.productSku()
			);
		} catch (org.springframework.dao.EmptyResultDataAccessException e) {
			// Товар не найден в справочнике лояльности — оставляем исходное значение
			loyaltyStatus = product.productData();
		}

		Product enrichedProduct = new Product(
			product.productId(),
			product.productSku(),
			product.productName(),
			product.productAmount(),
			loyaltyStatus
		);

		log.info("Transforming Product SKU ({}) -> Loyalty: {}", product.productSku(), loyaltyStatus);
		return enrichedProduct;
	}

}
