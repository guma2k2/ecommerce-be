package com.yas.system.catalog;

import com.yas.system.catalog.internal.dto.request.*;
import com.yas.system.catalog.internal.entity.Brand;
import com.yas.system.catalog.internal.entity.Category;
import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.entity.attribute.ProductAttribute;
import com.yas.system.catalog.internal.entity.option.ProductOption;
import com.yas.system.catalog.internal.entity.variant.ProductVariant;
import com.yas.system.catalog.internal.entity.variant.VariantOptionValue;
import com.yas.system.catalog.internal.repository.*;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.media.internal.entity.Media;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("POST /api/v1/products - Create Product Integration Tests")
public class ProductCreateApiIT extends AbstractIntegrationTest {

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
    private ProductOption testOption;
    private ProductAttribute testAttribute;
    private Media testMedia;

    @BeforeEach
    void setUp() {
        fixture.cleanDatabase();
        testCategory = fixture.createCategory("Electronics");
        testBrand = fixture.createBrand("Apple");
        testOption = fixture.createProductOption("Color");
        testAttribute = fixture.createProductAttribute("Material");
        testMedia = fixture.createMedia("test-image", "https://media.example.com/test-image.jpg");
    }

    private ProductVariantCreateRequest defaultVariant(String sku) {
        return new ProductVariantCreateRequest(
                "Standard Variant",
                sku,
                new BigDecimal("100.00"),
                10,
                null,
                null
        );
    }

    private ProductCreateRequest defaultCreateRequest(String name, String slug) {
        return new ProductCreateRequest(
                name,
                "Description for " + name,
                slug,
                "Meta " + name,
                "keyword, " + name,
                "Meta desc for " + name,
                null,
                null,
                null,
                null,
                null,
                List.of(defaultVariant(slug + "-SKU-1"))
        );
    }

    @Nested
    @DisplayName("1.1 Validation / Error Cases")
    class ValidationErrors {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("V1 - name already exists in DB -> PRODUCT_NAME_ALREADY_EXISTS")
        void v1_nameAlreadyExists() throws Exception {
            fixture.createSeedProductWithFullRelations(
                    "Duplicate Phone",
                    "duplicate-phone-1",
                    testCategory,
                    testBrand,
                    testMedia,
                    testOption,
                    testAttribute
            );

            ProductCreateRequest request = defaultCreateRequest("Duplicate Phone", "new-slug");

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("400"))
                    .andExpect(jsonPath("$.data").value(ErrorCode.PRODUCT_NAME_ALREADY_EXISTS.getCode()));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("V2 - slug already exists in DB -> PRODUCT_SLUG_ALREADY_EXISTS")
        void v2_slugAlreadyExists() throws Exception {
            fixture.createSeedProductWithFullRelations(
                    "Existing Phone",
                    "duplicate-slug",
                    testCategory,
                    testBrand,
                    testMedia,
                    testOption,
                    testAttribute
            );

            ProductCreateRequest request = defaultCreateRequest("New Phone", "duplicate-slug");

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("400"))
                    .andExpect(jsonPath("$.data").value(ErrorCode.PRODUCT_SLUG_ALREADY_EXISTS.getCode()));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("V3 - categoryId not found in DB -> CATEGORY_NOT_FOUND")
        void v3_categoryNotFound() throws Exception {
            ProductCreateRequest request = new ProductCreateRequest(
                    "Invalid Cat Phone", "Desc", "invalid-cat-phone", null, null, null,
                    999999, null, null, null, null,
                    List.of(defaultVariant("SKU-V3"))
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("400"))
                    .andExpect(jsonPath("$.data").value(ErrorCode.CATEGORY_NOT_FOUND.getCode()));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("V4 - brandId not found in DB -> BRAND_NOT_FOUND")
        void v4_brandNotFound() throws Exception {
            ProductCreateRequest request = new ProductCreateRequest(
                    "Invalid Brand Phone", "Desc", "invalid-brand-phone", null, null, null,
                    null, 999999, null, null, null,
                    List.of(defaultVariant("SKU-V4"))
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("400"))
                    .andExpect(jsonPath("$.data").value(ErrorCode.BRAND_NOT_FOUND.getCode()));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("V5 - attributes[].productAttributeId not found in DB -> PRODUCT_ATTRIBUTE_NOT_FOUND")
        void v5_productAttributeNotFound() throws Exception {
            ProductCreateRequest request = new ProductCreateRequest(
                    "Attr Not Found", "Desc", "attr-not-found", null, null, null,
                    null, null, null, null,
                    List.of(new ProductAttributeValueCreateRequest(999999L, "Some Value")),
                    List.of(defaultVariant("SKU-V5"))
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("400"))
                    .andExpect(jsonPath("$.data").value(ErrorCode.PRODUCT_ATTRIBUTE_NOT_FOUND.getCode()));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("V6 - options[].productOptionId not found in DB -> PRODUCT_OPTION_NOT_FOUND")
        void v6_productOptionNotFound() throws Exception {
            ProductCreateRequest request = new ProductCreateRequest(
                    "Opt Not Found", "Desc", "opt-not-found", null, null, null,
                    null, null, null,
                    List.of(new ProductOptionCombinationCreateRequest(999999L, 1, List.of())),
                    null,
                    List.of(defaultVariant("SKU-V6"))
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("400"))
                    .andExpect(jsonPath("$.data").value(ErrorCode.PRODUCT_OPTION_NOT_FOUND.getCode()));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("V7 - variants[].attributeValues[].productAttributeId not found -> PRODUCT_ATTRIBUTE_NOT_FOUND")
        void v7_variantAttributeNotFound() throws Exception {
            ProductVariantCreateRequest variantWithInvalidAttr = new ProductVariantCreateRequest(
                    "Variant 1", "SKU-V7", new BigDecimal("50.00"), 5, null,
                    List.of(new ProductVariantAttributeValueCreateRequest(999999L, "Val"))
            );

            ProductCreateRequest request = new ProductCreateRequest(
                    "Var Attr Not Found", "Desc", "var-attr-not-found", null, null, null,
                    null, null, null, null, null,
                    List.of(variantWithInvalidAttr)
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("400"))
                    .andExpect(jsonPath("$.data").value(ErrorCode.PRODUCT_ATTRIBUTE_NOT_FOUND.getCode()));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("V8 - Required fields missing (blank name/slug or empty variants) -> BAD_REQUEST")
        void v8_beanValidationFailure() throws Exception {
            // Missing name and variants
            ProductCreateRequest invalidRequest = new ProductCreateRequest(
                    "", "Desc", "", null, null, null,
                    null, null, null, null, null,
                    List.of()
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("400"))
                    .andExpect(jsonPath("$.data").value(ErrorCode.BAD_REQUEST.getCode()));
        }
    }

    @Nested
    @DisplayName("1.2 Base Product Fields")
    class BaseProductFields {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("B1 - categoryId = null -> saved with category = null")
        void b1_categoryNull() throws Exception {
            ProductCreateRequest request = defaultCreateRequest("Category Null", "cat-null");

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("200"))
                    .andExpect(jsonPath("$.data.category").doesNotExist());

            Product saved = fixture.findProductBySlug("cat-null");
            assertThat(saved.getCategory()).isNull();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("B2 - categoryId valid -> saved with Category entity")
        void b2_categoryValid() throws Exception {
            ProductCreateRequest request = new ProductCreateRequest(
                    "Cat Valid", "Desc", "cat-valid", null, null, null,
                    testCategory.getId(), null, null, null, null,
                    List.of(defaultVariant("SKU-B2"))
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("200"))
                    .andExpect(jsonPath("$.data.category.id").value(testCategory.getId()));

            Product saved = fixture.findProductBySlug("cat-valid");
            assertThat(saved.getCategory()).isNotNull();
            assertThat(saved.getCategory().getId()).isEqualTo(testCategory.getId());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("B3 - brandId = null -> saved with brand = null")
        void b3_brandNull() throws Exception {
            ProductCreateRequest request = defaultCreateRequest("Brand Null", "brand-null");

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("200"))
                    .andExpect(jsonPath("$.data.brand").doesNotExist());

            Product saved = fixture.findProductBySlug("brand-null");
            assertThat(saved.getBrand()).isNull();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("B4 - brandId valid -> saved with Brand entity")
        void b4_brandValid() throws Exception {
            ProductCreateRequest request = new ProductCreateRequest(
                    "Brand Valid", "Desc", "brand-valid", null, null, null,
                    null, testBrand.getId(), null, null, null,
                    List.of(defaultVariant("SKU-B4"))
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("200"))
                    .andExpect(jsonPath("$.data.brand.id").value(testBrand.getId()));

            Product saved = fixture.findProductBySlug("brand-valid");
            assertThat(saved.getBrand()).isNotNull();
            assertThat(saved.getBrand().getId()).isEqualTo(testBrand.getId());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("B5 - Both categoryId and brandId valid -> both saved")
        void b5_categoryAndBrandValid() throws Exception {
            ProductCreateRequest request = new ProductCreateRequest(
                    "Cat And Brand", "Desc", "cat-and-brand", null, null, null,
                    testCategory.getId(), testBrand.getId(), null, null, null,
                    List.of(defaultVariant("SKU-B5"))
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("200"))
                    .andExpect(jsonPath("$.data.category.id").value(testCategory.getId()))
                    .andExpect(jsonPath("$.data.brand.id").value(testBrand.getId()));

            Product saved = fixture.findProductBySlug("cat-and-brand");
            assertThat(saved.getCategory()).isNotNull();
            assertThat(saved.getBrand()).isNotNull();
        }
    }

    @Nested
    @DisplayName("1.3 Media")
    class MediaTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("M1 - medias = null or empty -> 0 ProductMedia rows inserted")
        void m1_mediasNullOrEmpty() throws Exception {
            ProductCreateRequest request = defaultCreateRequest("No Media", "no-media");

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.medias").isEmpty());

            Product saved = fixture.findProductBySlug("no-media");
            assertThat(productMediaRepository.findByProductIdOrderByPositionAsc(saved.getId())).isEmpty();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("M2 - 1 media provided -> 1 ProductMedia row and URL resolved")
        void m2_singleMedia() throws Exception {
            ProductCreateRequest request = new ProductCreateRequest(
                    "Single Media", "Desc", "single-media", null, null, null,
                    null, null,
                    List.of(new ProductMediaRequest(testMedia.getId().toString(), 1)),
                    null, null,
                    List.of(defaultVariant("SKU-M2"))
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.medias", hasSize(1)))
                    .andExpect(jsonPath("$.data.medias[0].media_id").value(testMedia.getId().toString()))
                    .andExpect(jsonPath("$.data.medias[0].url").value(testMedia.getUrl()));

            Product saved = fixture.findProductBySlug("single-media");
            assertThat(productMediaRepository.findByProductIdOrderByPositionAsc(saved.getId())).hasSize(1);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("M3 - N medias provided -> N ProductMedia rows, URLs returned")
        void m3_multipleMedias() throws Exception {
            Media media2 = fixture.createMedia("image2", "https://media.example.com/2.jpg");

            ProductCreateRequest request = new ProductCreateRequest(
                    "Multi Media", "Desc", "multi-media", null, null, null,
                    null, null,
                    List.of(
                            new ProductMediaRequest(testMedia.getId().toString(), 1),
                            new ProductMediaRequest(media2.getId().toString(), 2)
                    ),
                    null, null,
                    List.of(defaultVariant("SKU-M3"))
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.medias", hasSize(2)));

            Product saved = fixture.findProductBySlug("multi-media");
            assertThat(productMediaRepository.findByProductIdOrderByPositionAsc(saved.getId())).hasSize(2);
        }
    }

    @Nested
    @DisplayName("1.4 Options (ProductOptionCombination + ProductOptionValue)")
    class OptionTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("O1/O2 - options = null or empty -> no combinations/values saved")
        void o1_optionsEmpty() throws Exception {
            ProductCreateRequest request = defaultCreateRequest("No Options", "no-options");

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.options").isEmpty());

            Product saved = fixture.findProductBySlug("no-options");
            assertThat(optionCombinationRepository.findByProductIdOrderByPositionAsc(saved.getId())).isEmpty();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("O3 - 1 option, 0 values -> 1 combination saved, 0 option values")
        void o3_oneOptionZeroValues() throws Exception {
            ProductCreateRequest request = new ProductCreateRequest(
                    "Opt Zero Val", "Desc", "opt-zero-val", null, null, null,
                    null, null, null,
                    List.of(new ProductOptionCombinationCreateRequest(testOption.getId(), 1, List.of())),
                    null,
                    List.of(defaultVariant("SKU-O3"))
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.options", hasSize(1)))
                    .andExpect(jsonPath("$.data.options[0].values").isEmpty());

            Product saved = fixture.findProductBySlug("opt-zero-val");
            assertThat(optionCombinationRepository.findByProductIdOrderByPositionAsc(saved.getId())).hasSize(1);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("O4 - 1 option, N values -> 1 combination, N values linked")
        void o4_oneOptionMultipleValues() throws Exception {
            ProductCreateRequest request = new ProductCreateRequest(
                    "Opt Multi Val", "Desc", "opt-multi-val", null, null, null,
                    null, null, null,
                    List.of(new ProductOptionCombinationCreateRequest(
                            testOption.getId(), 1,
                            List.of(
                                    new ProductOptionValueCreateRequest("Red", 1),
                                    new ProductOptionValueCreateRequest("Blue", 2),
                                    new ProductOptionValueCreateRequest("Green", 3)
                            )
                    )),
                    null,
                    List.of(defaultVariant("SKU-O4"))
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.options", hasSize(1)))
                    .andExpect(jsonPath("$.data.options[0].values", hasSize(3)));

            Product saved = fixture.findProductBySlug("opt-multi-val");
            assertThat(optionCombinationRepository.findByProductIdOrderByPositionAsc(saved.getId())).hasSize(1);
            assertThat(optionValueRepository.findByProductId(saved.getId())).hasSize(3);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("O5 - N options, each with M values -> N combinations, N*M values")
        void o5_multiOptionsMultiValues() throws Exception {
            ProductOption sizeOption = fixture.createProductOption("Size");

            ProductCreateRequest request = new ProductCreateRequest(
                    "Multi Opt Multi Val", "Desc", "multi-opt-multi-val", null, null, null,
                    null, null, null,
                    List.of(
                            new ProductOptionCombinationCreateRequest(
                                    testOption.getId(), 1,
                                    List.of(
                                            new ProductOptionValueCreateRequest("Red", 1),
                                            new ProductOptionValueCreateRequest("Blue", 2)
                                    )
                            ),
                            new ProductOptionCombinationCreateRequest(
                                    sizeOption.getId(), 2,
                                    List.of(
                                            new ProductOptionValueCreateRequest("Small", 1),
                                            new ProductOptionValueCreateRequest("Medium", 2),
                                            new ProductOptionValueCreateRequest("Large", 3)
                                    )
                            )
                    ),
                    null,
                    List.of(defaultVariant("SKU-O5"))
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.options", hasSize(2)))
                    .andExpect(jsonPath("$.data.options[0].values", hasSize(2)))
                    .andExpect(jsonPath("$.data.options[1].values", hasSize(3)));

            Product saved = fixture.findProductBySlug("multi-opt-multi-val");
            assertThat(optionCombinationRepository.findByProductIdOrderByPositionAsc(saved.getId())).hasSize(2);
            assertThat(optionValueRepository.findByProductId(saved.getId())).hasSize(5);
        }
    }

    @Nested
    @DisplayName("1.5 Attributes (ProductAttributeValue)")
    class AttributeTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("A1/A2 - attributes = null or empty -> 0 ProductAttributeValue rows")
        void a1_attributesEmpty() throws Exception {
            ProductCreateRequest request = defaultCreateRequest("No Attributes", "no-attributes");

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.attributes").isEmpty());

            Product saved = fixture.findProductBySlug("no-attributes");
            assertThat(attributeValueRepository.findByProductId(saved.getId())).isEmpty();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("A3 - 1 attribute provided -> 1 ProductAttributeValue row")
        void a3_oneAttribute() throws Exception {
            ProductCreateRequest request = new ProductCreateRequest(
                    "One Attr", "Desc", "one-attr", null, null, null,
                    null, null, null, null,
                    List.of(new ProductAttributeValueCreateRequest(testAttribute.getId(), "100% Wool")),
                    List.of(defaultVariant("SKU-A3"))
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.attributes", hasSize(1)))
                    .andExpect(jsonPath("$.data.attributes[0].value").value("100% Wool"));

            Product saved = fixture.findProductBySlug("one-attr");
            assertThat(attributeValueRepository.findByProductId(saved.getId())).hasSize(1);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("A4 - N attributes provided -> N ProductAttributeValue rows")
        void a4_multipleAttributes() throws Exception {
            ProductAttribute brandAttr = fixture.createProductAttribute("Origin");

            ProductCreateRequest request = new ProductCreateRequest(
                    "Multi Attr", "Desc", "multi-attr", null, null, null,
                    null, null, null, null,
                    List.of(
                            new ProductAttributeValueCreateRequest(testAttribute.getId(), "Silk"),
                            new ProductAttributeValueCreateRequest(brandAttr.getId(), "Italy")
                    ),
                    List.of(defaultVariant("SKU-A4"))
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.attributes", hasSize(2)));

            Product saved = fixture.findProductBySlug("multi-attr");
            assertThat(attributeValueRepository.findByProductId(saved.getId())).hasSize(2);
        }
    }

    @Nested
    @DisplayName("1.6 Variants")
    class VariantTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("VR3 - 1 variant, no options -> 1 ProductVariant, 0 VariantOptionValue")
        void vr3_oneVariantNoOptions() throws Exception {
            ProductCreateRequest request = defaultCreateRequest("Variant No Opt", "var-no-opt");

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.variants", hasSize(1)));

            Product saved = fixture.findProductBySlug("var-no-opt");
            List<ProductVariant> variants = productVariantRepository.findByProductId(saved.getId());
            assertThat(variants).hasSize(1);
            assertThat(variantOptionValueRepository.findByProductVariantProductId(saved.getId())).isEmpty();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("VR4 - 1 variant with options -> VariantOptionValue linked")
        void vr4_oneVariantWithOptions() throws Exception {
            ProductVariantCreateRequest variant = new ProductVariantCreateRequest(
                    "Standard Variant", "SKU-VR4", new BigDecimal("100.00"), 10, null,
                    List.of(new ProductVariantOptionValueCreateRequest(testOption.getId(), "Black")),
                    null
            );

            ProductCreateRequest request = new ProductCreateRequest(
                    "Var With Opt", "Desc", "var-with-opt", null, null, null,
                    null, null, null,
                    List.of(new ProductOptionCombinationCreateRequest(
                            testOption.getId(), 1,
                            List.of(new ProductOptionValueCreateRequest("Black", 1))
                    )),
                    null,
                    List.of(variant)
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.variants", hasSize(1)));

            Product saved = fixture.findProductBySlug("var-with-opt");
            List<ProductVariant> variants = productVariantRepository.findByProductId(saved.getId());
            assertThat(variants).hasSize(1);
            assertThat(variantOptionValueRepository.findByProductVariantProductId(saved.getId())).hasSize(1);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("VR5 - N variants with options -> N ProductVariants linked to options")
        void vr5_multipleVariantsWithOptions() throws Exception {
            ProductVariantCreateRequest variantRed = new ProductVariantCreateRequest(
                    "Standard Variant", "SKU-VR5-RED", new BigDecimal("100.00"), 10, null,
                    List.of(new ProductVariantOptionValueCreateRequest(testOption.getId(), "Red")),
                    null
            );
            ProductVariantCreateRequest variantBlue = new ProductVariantCreateRequest(
                    "Standard Variant", "SKU-VR5-BLUE", new BigDecimal("100.00"), 10, null,
                    List.of(new ProductVariantOptionValueCreateRequest(testOption.getId(), "Blue")),
                    null
            );

            ProductCreateRequest request = new ProductCreateRequest(
                    "Multi Var Multi Opt", "Desc", "multi-var-multi-opt", null, null, null,
                    null, null, null,
                    List.of(new ProductOptionCombinationCreateRequest(
                            testOption.getId(), 1,
                            List.of(
                                    new ProductOptionValueCreateRequest("Red", 1),
                                    new ProductOptionValueCreateRequest("Blue", 2)
                            )
                    )),
                    null,
                    List.of(variantRed, variantBlue)
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.variants", hasSize(2)));

            Product saved = fixture.findProductBySlug("multi-var-multi-opt");
            List<ProductVariant> variants = productVariantRepository.findByProductId(saved.getId());
            assertThat(variants).hasSize(2);
            assertThat(variantOptionValueRepository.findByProductVariantProductId(saved.getId())).hasSize(2);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("VR6 - Variant with no attributeValues -> 0 ProductVariantAttributeValue")
        void vr6_variantNoAttributes() throws Exception {
            ProductCreateRequest request = defaultCreateRequest("Var No Attr", "var-no-attr");

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            Product saved = fixture.findProductBySlug("var-no-attr");
            ProductVariant variant = productVariantRepository.findByProductId(saved.getId()).getFirst();
            assertThat(variantAttributeValueRepository.findByProductVariantId(variant.getId())).isEmpty();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("VR7 - Variant with attributeValues -> N ProductVariantAttributeValue rows")
        void vr7_variantWithAttributes() throws Exception {
            ProductVariantCreateRequest variantWithAttr = new ProductVariantCreateRequest(
                    "Var With Attr", "SKU-VR7", new BigDecimal("120.00"), 5, null,
                    List.of(new ProductVariantAttributeValueCreateRequest(testAttribute.getId(), "Heavyweight"))
            );

            ProductCreateRequest request = new ProductCreateRequest(
                    "Product VR7", "Desc", "product-vr7", null, null, null,
                    null, null, null, null, null,
                    List.of(variantWithAttr)
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.variants[0].attribute_values", hasSize(1)))
                    .andExpect(jsonPath("$.data.variants[0].attribute_values[0].value").value("Heavyweight"));

            Product saved = fixture.findProductBySlug("product-vr7");
            ProductVariant variant = productVariantRepository.findByProductId(saved.getId()).getFirst();
            assertThat(variantAttributeValueRepository.findByProductVariantId(variant.getId())).hasSize(1);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("VR8 - variant.title null/blank -> defaults to 'Default Title'")
        void vr8_variantTitleDefaulted() throws Exception {
            ProductVariantCreateRequest variantBlankTitle = new ProductVariantCreateRequest(
                    null, "SKU-VR8", new BigDecimal("45.00"), 10, null, null
            );

            ProductCreateRequest request = new ProductCreateRequest(
                    "Var Default Title", "Desc", "var-default-title", null, null, null,
                    null, null, null, null, null,
                    List.of(variantBlankTitle)
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.variants[0].title").value("Default Title"));

            Product saved = fixture.findProductBySlug("var-default-title");
            ProductVariant variant = productVariantRepository.findByProductId(saved.getId()).getFirst();
            assertThat(variant.getTitle()).isEqualTo("Default Title");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("VR9 - Variant with mediaId -> URL resolved in response")
        void vr9_variantWithMedia() throws Exception {
            ProductVariantCreateRequest variantWithMedia = new ProductVariantCreateRequest(
                    "Variant Media", "SKU-VR9", new BigDecimal("75.00"), 8, testMedia.getId().toString(), null
            );

            ProductCreateRequest request = new ProductCreateRequest(
                    "Var Media Prod", "Desc", "var-media-prod", null, null, null,
                    null, null, null, null, null,
                    List.of(variantWithMedia)
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.variants[0].media_url").value(testMedia.getUrl()));

            Product saved = fixture.findProductBySlug("var-media-prod");
            ProductVariant variant = productVariantRepository.findByProductId(saved.getId()).getFirst();
            assertThat(variant.getMediaId()).isEqualTo(testMedia.getId().toString());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("VR10 - Cartesian product multi-option variants (e.g. Color x Size) -> all VariantOptionValues linked")
        void vr10_multiOptionCartesianProductVariants() throws Exception {
            ProductOption sizeOption = fixture.createProductOption("Size");

            ProductOptionCombinationCreateRequest colorOptReq = new ProductOptionCombinationCreateRequest(
                    testOption.getId(), 1,
                    List.of(
                            new ProductOptionValueCreateRequest("Red", 1),
                            new ProductOptionValueCreateRequest("Blue", 2)
                    )
            );
            ProductOptionCombinationCreateRequest sizeOptReq = new ProductOptionCombinationCreateRequest(
                    sizeOption.getId(), 2,
                    List.of(
                            new ProductOptionValueCreateRequest("S", 1),
                            new ProductOptionValueCreateRequest("M", 2)
                    )
            );

            ProductVariantCreateRequest vRedS = new ProductVariantCreateRequest(
                    "Red - S", "SKU-RED-S", new BigDecimal("50.00"), 10, null,
                    List.of(
                            new ProductVariantOptionValueCreateRequest(testOption.getId(), "Red"),
                            new ProductVariantOptionValueCreateRequest(sizeOption.getId(), "S")
                    ),
                    null
            );
            ProductVariantCreateRequest vRedM = new ProductVariantCreateRequest(
                    "Red - M", "SKU-RED-M", new BigDecimal("50.00"), 15, null,
                    List.of(
                            new ProductVariantOptionValueCreateRequest(testOption.getId(), "Red"),
                            new ProductVariantOptionValueCreateRequest(sizeOption.getId(), "M")
                    ),
                    null
            );
            ProductVariantCreateRequest vBlueS = new ProductVariantCreateRequest(
                    "Blue - S", "SKU-BLUE-S", new BigDecimal("50.00"), 20, null,
                    List.of(
                            new ProductVariantOptionValueCreateRequest(testOption.getId(), "Blue"),
                            new ProductVariantOptionValueCreateRequest(sizeOption.getId(), "S")
                    ),
                    null
            );
            ProductVariantCreateRequest vBlueM = new ProductVariantCreateRequest(
                    "Blue - M", "SKU-BLUE-M", new BigDecimal("50.00"), 25, null,
                    List.of(
                            new ProductVariantOptionValueCreateRequest(testOption.getId(), "Blue"),
                            new ProductVariantOptionValueCreateRequest(sizeOption.getId(), "M")
                    ),
                    null
            );

            ProductCreateRequest request = new ProductCreateRequest(
                    "Multi Option Cartesian", "Desc", "multi-option-cartesian", null, null, null,
                    null, null, null,
                    List.of(colorOptReq, sizeOptReq),
                    null,
                    List.of(vRedS, vRedM, vBlueS, vBlueM)
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.variants", hasSize(4)))
                    .andExpect(jsonPath("$.data.variants[0].product_option_value_ids", hasSize(2)))
                    .andExpect(jsonPath("$.data.variants[1].product_option_value_ids", hasSize(2)))
                    .andExpect(jsonPath("$.data.variants[2].product_option_value_ids", hasSize(2)))
                    .andExpect(jsonPath("$.data.variants[3].product_option_value_ids", hasSize(2)));

            Product saved = fixture.findProductBySlug("multi-option-cartesian");
            List<VariantOptionValue> allVovs = variantOptionValueRepository.findByProductVariantProductId(saved.getId());
            assertThat(allVovs).hasSize(8);
        }
    }

    @Nested
    @DisplayName("1.7 Combined (Happy Path)")
    class CombinedHappyPaths {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("C1 - Full Product: category, brand, medias, options, attributes, variants with attributes")
        void c1_fullProductHappyPath() throws Exception {
            ProductCreateRequest request = new ProductCreateRequest(
                    "Full Product Pro",
                    "Complete product specifications",
                    "full-product-pro",
                    "Meta Title Pro",
                    "pro, full, ecommerce",
                    "Meta description for pro product",
                    testCategory.getId(),
                    testBrand.getId(),
                    List.of(new ProductMediaRequest(testMedia.getId().toString(), 1)),
                    List.of(new ProductOptionCombinationCreateRequest(
                            testOption.getId(), 1,
                            List.of(new ProductOptionValueCreateRequest("Space Gray", 1))
                    )),
                    List.of(new ProductAttributeValueCreateRequest(testAttribute.getId(), "Aluminum")),
                    List.of(new ProductVariantCreateRequest(
                            "Pro 256GB",
                            "PRO-256-GRAY",
                            new BigDecimal("999.99"),
                            25,
                            testMedia.getId().toString(),
                            List.of(new ProductVariantAttributeValueCreateRequest(testAttribute.getId(), "Matte Finish"))
                    ))
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("200"))
                    .andExpect(jsonPath("$.data.name").value("Full Product Pro"))
                    .andExpect(jsonPath("$.data.slug").value("full-product-pro"))
                    .andExpect(jsonPath("$.data.category.id").value(testCategory.getId()))
                    .andExpect(jsonPath("$.data.brand.id").value(testBrand.getId()))
                    .andExpect(jsonPath("$.data.medias", hasSize(1)))
                    .andExpect(jsonPath("$.data.options", hasSize(1)))
                    .andExpect(jsonPath("$.data.attributes", hasSize(1)))
                    .andExpect(jsonPath("$.data.variants", hasSize(1)))
                    .andExpect(jsonPath("$.data.variants[0].sku").value("PRO-256-GRAY"))
                    .andExpect(jsonPath("$.data.variants[0].media_url").value(testMedia.getUrl()))
                    .andExpect(jsonPath("$.data.variants[0].attribute_values", hasSize(1)));

            Product saved = fixture.findProductBySlug("full-product-pro");
            assertThat(saved.getCategory().getId()).isEqualTo(testCategory.getId());
            assertThat(saved.getBrand().getId()).isEqualTo(testBrand.getId());
            assertThat(productMediaRepository.findByProductIdOrderByPositionAsc(saved.getId())).hasSize(1);
            assertThat(optionCombinationRepository.findByProductIdOrderByPositionAsc(saved.getId())).hasSize(1);
            assertThat(attributeValueRepository.findByProductId(saved.getId())).hasSize(1);
            assertThat(productVariantRepository.findByProductId(saved.getId())).hasSize(1);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("C2 - Minimal Product: only name + slug + 1 basic variant")
        void c2_minimalProduct() throws Exception {
            ProductCreateRequest request = defaultCreateRequest("Minimal Product", "minimal-product");

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("200"))
                    .andExpect(jsonPath("$.data.name").value("Minimal Product"))
                    .andExpect(jsonPath("$.data.slug").value("minimal-product"))
                    .andExpect(jsonPath("$.data.medias").isEmpty())
                    .andExpect(jsonPath("$.data.options").isEmpty())
                    .andExpect(jsonPath("$.data.attributes").isEmpty())
                    .andExpect(jsonPath("$.data.variants", hasSize(1)));

            Product saved = fixture.findProductBySlug("minimal-product");
            assertThat(saved.getCategory()).isNull();
            assertThat(saved.getBrand()).isNull();
            assertThat(productMediaRepository.findByProductIdOrderByPositionAsc(saved.getId())).isEmpty();
            assertThat(optionCombinationRepository.findByProductIdOrderByPositionAsc(saved.getId())).isEmpty();
            assertThat(attributeValueRepository.findByProductId(saved.getId())).isEmpty();
            assertThat(productVariantRepository.findByProductId(saved.getId())).hasSize(1);
        }
    }
}
