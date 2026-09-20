package com.yas.system.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.yas.system.catalog.internal.dto.request.*;
import com.yas.system.catalog.internal.entity.Brand;
import com.yas.system.catalog.internal.entity.Category;
import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.entity.attribute.ProductAttribute;
import com.yas.system.catalog.internal.entity.option.ProductOption;
import com.yas.system.catalog.internal.repository.*;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.media.internal.entity.Media;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("DELETE /api/v1/products/{productId} - Delete Product Integration Tests")
public class ProductDeleteApiIT extends AbstractIntegrationTest {

    @Autowired
    private CatalogTestFixture fixture;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private ProductMediaRepository productMediaRepository;

    @Autowired
    private ProductOptionCombinationRepository optionCombinationRepository;

    @Autowired
    private ProductOptionValueRepository optionValueRepository;

    @Autowired
    private ProductAttributeValueRepository attributeValueRepository;

    @Autowired
    private VariantOptionValueRepository variantOptionValueRepository;

    @Autowired
    private ProductVariantAttributeValueRepository variantAttributeValueRepository;

    private Category testCategory;
    private Brand testBrand;
    private ProductOption testOptionColor;
    private ProductOption testOptionSize;
    private ProductAttribute testAttribute;
    private Media testMedia;

    @BeforeEach
    void setUp() {
        fixture.cleanDatabase();
        testCategory = fixture.createCategory("Electronics");
        testBrand = fixture.createBrand("Apple");
        testOptionColor = fixture.createProductOption("Color");
        testOptionSize = fixture.createProductOption("Size");
        testAttribute = fixture.createProductAttribute("Material");
        testMedia = fixture.createMedia("test-image", "https://media.example.com/test-image.jpg");
    }

    private ProductCreateRequest buildSimpleProductRequest(String name, String slug) {
        return new ProductCreateRequest(
                name,
                "Simple product description",
                slug,
                "Meta title",
                "keyword",
                "Meta description",
                testCategory.getId(),
                testBrand.getId(),
                null,
                null,
                null,
                List.of(new ProductVariantCreateRequest(
                        "Default Variant",
                        slug + "-SKU-1",
                        BigDecimal.valueOf(99.99),
                        10,
                        null,
                        null,
                        null
                ))
        );
    }

    private ProductCreateRequest buildComplexProductRequest(String name, String slug) {
        return new ProductCreateRequest(
                name,
                "Full-featured complex product description",
                slug,
                "Complex Meta Title",
                "complex, product",
                "Complex meta description",
                testCategory.getId(),
                testBrand.getId(),
                List.of(new ProductMediaRequest(testMedia.getId().toString(), 0)),
                List.of(
                        new ProductOptionCombinationCreateRequest(
                                testOptionColor.getId(),
                                0,
                                List.of(
                                        new ProductOptionValueCreateRequest("Black", 0),
                                        new ProductOptionValueCreateRequest("White", 1)
                                )
                        ),
                        new ProductOptionCombinationCreateRequest(
                                testOptionSize.getId(),
                                1,
                                List.of(
                                        new ProductOptionValueCreateRequest("128GB", 0),
                                        new ProductOptionValueCreateRequest("256GB", 1)
                                )
                        )
                ),
                List.of(new ProductAttributeValueCreateRequest(testAttribute.getId(), "Aluminum")),
                List.of(
                        new ProductVariantCreateRequest(
                                "Black 128GB",
                                slug + "-BLK-128",
                                BigDecimal.valueOf(999.99),
                                50,
                                null,
                                List.of(
                                        new ProductVariantOptionValueCreateRequest(testOptionColor.getId(), "Black"),
                                        new ProductVariantOptionValueCreateRequest(testOptionSize.getId(), "128GB")
                                ),
                                List.of(new ProductVariantAttributeValueCreateRequest(testAttribute.getId(), "Matte Finish"))
                        ),
                        new ProductVariantCreateRequest(
                                "White 256GB",
                                slug + "-WHT-256",
                                BigDecimal.valueOf(1099.99),
                                30,
                                null,
                                List.of(
                                        new ProductVariantOptionValueCreateRequest(testOptionColor.getId(), "White"),
                                        new ProductVariantOptionValueCreateRequest(testOptionSize.getId(), "256GB")
                                ),
                                List.of(new ProductVariantAttributeValueCreateRequest(testAttribute.getId(), "Gloss Finish"))
                        )
                )
        );
    }

    private Long createProductViaApi(ProductCreateRequest request) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("del1_whenProductNotFound_shouldReturnProductNotFound")
    void del1_whenProductNotFound_shouldReturnProductNotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/products/999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("400")))
                .andExpect(jsonPath("$.data", is(ErrorCode.PRODUCT_NOT_FOUND.getCode())))
                .andExpect(jsonPath("$.message", is(ErrorCode.PRODUCT_NOT_FOUND.getMessage())));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("del2_simpleProduct_shouldDeleteSuccessfully")
    void del2_simpleProduct_shouldDeleteSuccessfully() throws Exception {
        // Arrange
        Long productId = createProductViaApi(buildSimpleProductRequest("Simple Product", "simple-product"));
        assertThat(productRepository.findById(productId)).isPresent();
        assertThat(productVariantRepository.findByProductId(productId)).hasSize(1);

        // Act
        mockMvc.perform(delete("/api/v1/products/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("204")));

        // Assert
        assertThat(productRepository.findById(productId)).isEmpty();
        assertThat(productVariantRepository.findByProductId(productId)).isEmpty();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("del3_seededProductWithFullRelations_shouldDeleteSuccessfullyWithoutConstraintViolation")
    void del3_seededProductWithFullRelations_shouldDeleteSuccessfullyWithoutConstraintViolation() throws Exception {
        // Arrange: Seed product with all relations using test fixture
        Product seededProduct = fixture.createSeedProductWithFullRelations(
                "Seeded Full Product",
                "seeded-full-product",
                testCategory,
                testBrand,
                testMedia,
                testOptionColor,
                testAttribute
        );
        Long productId = seededProduct.getId();

        // Verify initial state
        assertThat(productRepository.findById(productId)).isPresent();
        assertThat(productMediaRepository.findByProductIdOrderByPositionAsc(productId)).hasSize(1);
        assertThat(attributeValueRepository.findByProductId(productId)).hasSize(1);
        assertThat(optionCombinationRepository.findByProductIdOrderByPositionAsc(productId)).hasSize(1);
        assertThat(optionValueRepository.findByProductId(productId)).hasSize(1);
        assertThat(productVariantRepository.findByProductId(productId)).hasSize(1);
        assertThat(variantOptionValueRepository.findByProductVariantProductId(productId)).hasSize(1);
        assertThat(variantAttributeValueRepository.findByProductVariantProductId(productId)).hasSize(1);

        // Act: Delete product - must cascade delete without FK violations
        mockMvc.perform(delete("/api/v1/products/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("204")));

        // Assert: All child records and the parent product are deleted
        assertThat(productRepository.findById(productId)).isEmpty();
        assertThat(productMediaRepository.findByProductIdOrderByPositionAsc(productId)).isEmpty();
        assertThat(attributeValueRepository.findByProductId(productId)).isEmpty();
        assertThat(optionCombinationRepository.findByProductIdOrderByPositionAsc(productId)).isEmpty();
        assertThat(optionValueRepository.findByProductId(productId)).isEmpty();
        assertThat(productVariantRepository.findByProductId(productId)).isEmpty();
        assertThat(variantOptionValueRepository.findByProductVariantProductId(productId)).isEmpty();
        assertThat(variantAttributeValueRepository.findByProductVariantProductId(productId)).isEmpty();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("del4_complexProductCreatedViaApi_shouldDeleteSuccessfullyWithoutConstraintViolation")
    void del4_complexProductCreatedViaApi_shouldDeleteSuccessfullyWithoutConstraintViolation() throws Exception {
        // Arrange: Create complex product via POST API
        Long productId = createProductViaApi(buildComplexProductRequest("iPhone 16 Pro", "iphone-16-pro"));

        // Verify initial state
        assertThat(productRepository.findById(productId)).isPresent();
        assertThat(productMediaRepository.findByProductIdOrderByPositionAsc(productId)).hasSize(1);
        assertThat(attributeValueRepository.findByProductId(productId)).hasSize(1);
        assertThat(optionCombinationRepository.findByProductIdOrderByPositionAsc(productId)).hasSize(2);
        assertThat(optionValueRepository.findByProductId(productId)).hasSize(4);
        assertThat(productVariantRepository.findByProductId(productId)).hasSize(2);
        assertThat(variantOptionValueRepository.findByProductVariantProductId(productId)).hasSize(4);
        assertThat(variantAttributeValueRepository.findByProductVariantProductId(productId)).hasSize(2);

        // Act: Delete product via DELETE API
        mockMvc.perform(delete("/api/v1/products/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("204")));

        // Assert: All child records and parent product are completely deleted
        assertThat(productRepository.findById(productId)).isEmpty();
        assertThat(productMediaRepository.findByProductIdOrderByPositionAsc(productId)).isEmpty();
        assertThat(attributeValueRepository.findByProductId(productId)).isEmpty();
        assertThat(optionCombinationRepository.findByProductIdOrderByPositionAsc(productId)).isEmpty();
        assertThat(optionValueRepository.findByProductId(productId)).isEmpty();
        assertThat(productVariantRepository.findByProductId(productId)).isEmpty();
        assertThat(variantOptionValueRepository.findByProductVariantProductId(productId)).isEmpty();
        assertThat(variantAttributeValueRepository.findByProductVariantProductId(productId)).isEmpty();
    }
}
