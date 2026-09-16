package com.yas.system.catalog;

import com.yas.system.catalog.internal.dto.request.*;
import com.yas.system.catalog.internal.entity.Brand;
import com.yas.system.catalog.internal.entity.Category;
import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.entity.ProductMedia;
import com.yas.system.catalog.internal.entity.attribute.ProductAttribute;
import com.yas.system.catalog.internal.entity.attribute.ProductAttributeValue;
import com.yas.system.catalog.internal.entity.attribute.ProductVariantAttributeValue;
import com.yas.system.catalog.internal.entity.option.ProductOption;
import com.yas.system.catalog.internal.entity.option.ProductOptionCombination;
import com.yas.system.catalog.internal.entity.option.ProductOptionValue;
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
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("PUT /api/v1/products/{productId} - Update Product Integration Tests")
public class ProductUpdateApiIT extends AbstractIntegrationTest {

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
    private Product seededProduct;

    @BeforeEach
    void setUp() {
        fixture.cleanDatabase();
        testCategory = fixture.createCategory("Electronics");
        testBrand = fixture.createBrand("Sony");
        testOption = fixture.createProductOption("Color");
        testAttribute = fixture.createProductAttribute("Warranty");
        testMedia = fixture.createMedia("initial-image", "https://media.example.com/initial.jpg");

        seededProduct = fixture.createSeedProductWithFullRelations(
                "Initial Product",
                "initial-product",
                testCategory,
                testBrand,
                testMedia,
                testOption,
                testAttribute
        );
    }

    private ProductVariant getSeededVariant() {
        return productVariantRepository.findByProductId(seededProduct.getId()).getFirst();
    }

    private ProductOptionCombination getSeededCombination() {
        return optionCombinationRepository.findByProductIdOrderByPositionAsc(seededProduct.getId()).getFirst();
    }

    private ProductOptionValue getSeededOptionValue() {
        return optionValueRepository.findByProductId(seededProduct.getId()).getFirst();
    }

    private ProductUpdateRequest defaultUpdateRequest(String name, String slug) {
        ProductVariant variant = getSeededVariant();
        return new ProductUpdateRequest(
                name,
                "Updated description",
                slug,
                "Updated meta title",
                "updated, meta",
                "Updated meta description",
                testCategory.getId(),
                testBrand.getId(),
                null,
                null,
                null,
                List.of(new ProductVariantUpdateRequest(
                        variant.getId(),
                        variant.getTitle(),
                        variant.getSku(),
                        variant.getPrice(),
                        variant.getQuantity(),
                        variant.getMediaId(),
                        null
                ))
        );
    }

    @Nested
    @DisplayName("2.1 Validation / Error Cases")
    class ValidationErrors {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("V1 - productId not found in DB -> PRODUCT_NOT_FOUND")
        void v1_productNotFound() throws Exception {
            ProductUpdateRequest request = defaultUpdateRequest("Non-Existent", "non-existent");

            mockMvc.perform(put("/api/v1/products/{id}", 999999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("400"))
                    .andExpect(jsonPath("$.data").value(ErrorCode.PRODUCT_NOT_FOUND.getCode()));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("V2 - name changed to one that exists on another product -> PRODUCT_NAME_ALREADY_EXISTS")
        void v2_nameConflictWithOtherProduct() throws Exception {
            Category cat2 = fixture.createCategory("Home");
            fixture.createSeedProductWithFullRelations("Another Product", "another-product", cat2, null, testMedia, testOption, testAttribute);

            ProductUpdateRequest request = defaultUpdateRequest("Another Product", "initial-product");

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("400"))
                    .andExpect(jsonPath("$.data").value(ErrorCode.PRODUCT_NAME_ALREADY_EXISTS.getCode()));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("V3 - slug changed to one that exists on another product -> PRODUCT_SLUG_ALREADY_EXISTS")
        void v3_slugConflictWithOtherProduct() throws Exception {
            Category cat2 = fixture.createCategory("Home");
            fixture.createSeedProductWithFullRelations("Another Phone", "existing-slug", cat2, null, testMedia, testOption, testAttribute);

            ProductUpdateRequest request = defaultUpdateRequest("Initial Product", "existing-slug");

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("400"))
                    .andExpect(jsonPath("$.data").value(ErrorCode.PRODUCT_SLUG_ALREADY_EXISTS.getCode()));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("V4 - Same name and slug as current product (self-exclusion) -> SUCCESS")
        void v4_selfExclusionSuccess() throws Exception {
            ProductUpdateRequest request = defaultUpdateRequest("Initial Product", "initial-product");

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("200"))
                    .andExpect(jsonPath("$.data.name").value("Initial Product"))
                    .andExpect(jsonPath("$.data.slug").value("initial-product"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("V5 - categoryId not found -> CATEGORY_NOT_FOUND")
        void v5_categoryNotFound() throws Exception {
            ProductVariant variant = getSeededVariant();
            ProductUpdateRequest request = new ProductUpdateRequest(
                    "Updated Name", "Desc", "initial-product", null, null, null,
                    999999, null, null, null, null,
                    List.of(new ProductVariantUpdateRequest(variant.getId(), variant.getTitle(), variant.getSku(), variant.getPrice(), 1, null, null))
            );

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("400"))
                    .andExpect(jsonPath("$.data").value(ErrorCode.CATEGORY_NOT_FOUND.getCode()));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("V6 - brandId not found -> BRAND_NOT_FOUND")
        void v6_brandNotFound() throws Exception {
            ProductVariant variant = getSeededVariant();
            ProductUpdateRequest request = new ProductUpdateRequest(
                    "Updated Name", "Desc", "initial-product", null, null, null,
                    null, 999999, null, null, null,
                    List.of(new ProductVariantUpdateRequest(variant.getId(), variant.getTitle(), variant.getSku(), variant.getPrice(), 1, null, null))
            );

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("400"))
                    .andExpect(jsonPath("$.data").value(ErrorCode.BRAND_NOT_FOUND.getCode()));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("V7 - Attribute ID not found in DB -> PRODUCT_ATTRIBUTE_NOT_FOUND")
        void v7_attributeNotFound() throws Exception {
            ProductVariant variant = getSeededVariant();
            ProductUpdateRequest request = new ProductUpdateRequest(
                    "Updated Name", "Desc", "initial-product", null, null, null,
                    null, null, null, null,
                    List.of(new ProductAttributeValueUpdateRequest(999999L, "1 Year")),
                    List.of(new ProductVariantUpdateRequest(variant.getId(), variant.getTitle(), variant.getSku(), variant.getPrice(), 1, null, null))
            );

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("400"))
                    .andExpect(jsonPath("$.data").value(ErrorCode.PRODUCT_ATTRIBUTE_NOT_FOUND.getCode()));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("V8 - Option ID not found in DB -> PRODUCT_OPTION_NOT_FOUND")
        void v8_optionNotFound() throws Exception {
            ProductVariant variant = getSeededVariant();
            ProductUpdateRequest request = new ProductUpdateRequest(
                    "Updated Name", "Desc", "initial-product", null, null, null,
                    null, null, null,
                    List.of(new ProductOptionCombinationUpdateRequest(999999L, 1, List.of())),
                    null,
                    List.of(new ProductVariantUpdateRequest(variant.getId(), variant.getTitle(), variant.getSku(), variant.getPrice(), 1, null, null))
            );

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("400"))
                    .andExpect(jsonPath("$.data").value(ErrorCode.PRODUCT_OPTION_NOT_FOUND.getCode()));
        }
    }

    @Nested
    @DisplayName("2.2 Base Product Fields (Delta)")
    class BaseProductDelta {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("B1 - Change name and slug -> Product entity updated")
        void b1_changeNameAndSlug() throws Exception {
            ProductUpdateRequest request = defaultUpdateRequest("Brand New Name", "brand-new-slug");

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("Brand New Name"))
                    .andExpect(jsonPath("$.data.slug").value("brand-new-slug"));

            Product updated = productRepository.findById(seededProduct.getId()).orElseThrow();
            assertThat(updated.getName()).isEqualTo("Brand New Name");
            assertThat(updated.getSlug()).isEqualTo("brand-new-slug");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("B2 - categoryId = null -> Product category removed")
        void b2_removeCategory() throws Exception {
            ProductVariant variant = getSeededVariant();
            ProductUpdateRequest request = new ProductUpdateRequest(
                    "No Category Prod", "Desc", "initial-product", null, null, null,
                    null, testBrand.getId(), null, null, null,
                    List.of(new ProductVariantUpdateRequest(variant.getId(), variant.getTitle(), variant.getSku(), variant.getPrice(), 1, null, null))
            );

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.category").doesNotExist());

            Product updated = productRepository.findById(seededProduct.getId()).orElseThrow();
            assertThat(updated.getCategory()).isNull();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("B3 - categoryId changed to another valid ID -> New category applied")
        void b3_changeCategory() throws Exception {
            Category newCategory = fixture.createCategory("Appliances");
            ProductVariant variant = getSeededVariant();

            ProductUpdateRequest request = new ProductUpdateRequest(
                    "Changed Cat Prod", "Desc", "initial-product", null, null, null,
                    newCategory.getId(), testBrand.getId(), null, null, null,
                    List.of(new ProductVariantUpdateRequest(variant.getId(), variant.getTitle(), variant.getSku(), variant.getPrice(), 1, null, null))
            );

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.category.id").value(newCategory.getId()));

            Product updated = productRepository.findById(seededProduct.getId()).orElseThrow();
            assertThat(updated.getCategory().getId()).isEqualTo(newCategory.getId());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("B4 - brandId = null -> Product brand removed")
        void b4_removeBrand() throws Exception {
            ProductVariant variant = getSeededVariant();
            ProductUpdateRequest request = new ProductUpdateRequest(
                    "No Brand Prod", "Desc", "initial-product", null, null, null,
                    testCategory.getId(), null, null, null, null,
                    List.of(new ProductVariantUpdateRequest(variant.getId(), variant.getTitle(), variant.getSku(), variant.getPrice(), 1, null, null))
            );

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.brand").doesNotExist());

            Product updated = productRepository.findById(seededProduct.getId()).orElseThrow();
            assertThat(updated.getBrand()).isNull();
        }
    }

    @Nested
    @DisplayName("2.3 Media (Delta)")
    class MediaDelta {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("MD1 - medias = null or empty -> all existing medias deleted")
        void md1_deleteExistingMedias() throws Exception {
            ProductUpdateRequest request = defaultUpdateRequest("No Media Prod", "initial-product");

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.medias").isEmpty());

            assertThat(productMediaRepository.findByProductIdOrderByPositionAsc(seededProduct.getId())).isEmpty();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("MD2 - Same medias re-sent -> preserved, not deleted")
        void md2_preserveSameMedia() throws Exception {
            ProductVariant variant = getSeededVariant();
            ProductUpdateRequest request = new ProductUpdateRequest(
                    "Same Media Prod", "Desc", "initial-product", null, null, null,
                    testCategory.getId(), testBrand.getId(),
                    List.of(new ProductMediaRequest(testMedia.getId().toString(), 1)),
                    null, null,
                    List.of(new ProductVariantUpdateRequest(variant.getId(), variant.getTitle(), variant.getSku(), variant.getPrice(), 1, null, null))
            );

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.medias", hasSize(1)));

            List<ProductMedia> medias = productMediaRepository.findByProductIdOrderByPositionAsc(seededProduct.getId());
            assertThat(medias).hasSize(1);
            assertThat(medias.getFirst().getMediaId()).isEqualTo(testMedia.getId().toString());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("MD3 - Add new media -> new media inserted")
        void md3_addNewMedia() throws Exception {
            Media media2 = fixture.createMedia("second-image", "https://media.example.com/2.jpg");
            ProductVariant variant = getSeededVariant();

            ProductUpdateRequest request = new ProductUpdateRequest(
                    "Add Media Prod", "Desc", "initial-product", null, null, null,
                    testCategory.getId(), testBrand.getId(),
                    List.of(
                            new ProductMediaRequest(testMedia.getId().toString(), 1),
                            new ProductMediaRequest(media2.getId().toString(), 2)
                    ),
                    null, null,
                    List.of(new ProductVariantUpdateRequest(variant.getId(), variant.getTitle(), variant.getSku(), variant.getPrice(), 1, null, null))
            );

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.medias", hasSize(2)));

            assertThat(productMediaRepository.findByProductIdOrderByPositionAsc(seededProduct.getId())).hasSize(2);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("MD4 - Remove one media -> omitted media deleted")
        void md4_removeOneMedia() throws Exception {
            Media media2 = fixture.createMedia("second-image", "https://media.example.com/2.jpg");
            productMediaRepository.save(ProductMedia.builder().product(seededProduct).mediaId(media2.getId().toString()).position(2).build());

            ProductVariant variant = getSeededVariant();
            ProductUpdateRequest request = new ProductUpdateRequest(
                    "Remove Media Prod", "Desc", "initial-product", null, null, null,
                    testCategory.getId(), testBrand.getId(),
                    List.of(new ProductMediaRequest(media2.getId().toString(), 1)),
                    null, null,
                    List.of(new ProductVariantUpdateRequest(variant.getId(), variant.getTitle(), variant.getSku(), variant.getPrice(), 1, null, null))
            );

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.medias", hasSize(1)))
                    .andExpect(jsonPath("$.data.medias[0].media_id").value(media2.getId().toString()));

            List<ProductMedia> medias = productMediaRepository.findByProductIdOrderByPositionAsc(seededProduct.getId());
            assertThat(medias).hasSize(1);
            assertThat(medias.getFirst().getMediaId()).isEqualTo(media2.getId().toString());
        }
    }

    @Nested
    @DisplayName("2.4 Options (Delta)")
    class OptionDelta {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("OD1 - options = null or empty -> combinations and values deleted")
        void od1_deleteAllOptions() throws Exception {
            ProductVariant variant = getSeededVariant();
            ProductUpdateRequest request = new ProductUpdateRequest(
                    "Delete Opt Prod", "Desc", "initial-product", null, null, null,
                    testCategory.getId(), testBrand.getId(),
                    null, null, null,
                    List.of(new ProductVariantUpdateRequest(variant.getId(), variant.getTitle(), variant.getSku(), variant.getPrice(), 1, null, null))
            );

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.options").isEmpty());

            assertThat(optionCombinationRepository.findByProductIdOrderByPositionAsc(seededProduct.getId())).isEmpty();
            assertThat(optionValueRepository.findByProductId(seededProduct.getId())).isEmpty();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("OD2 - Keep same options -> preserved, positions updated")
        void od2_keepSameOptions() throws Exception {
            ProductVariant variant = getSeededVariant();
            ProductOptionValue existingVal = getSeededOptionValue();

            ProductUpdateRequest request = new ProductUpdateRequest(
                    "Same Opt Prod", "Desc", "initial-product", null, null, null,
                    testCategory.getId(), testBrand.getId(),
                    null,
                    List.of(new ProductOptionCombinationUpdateRequest(
                            testOption.getId(), 2,
                            List.of(new ProductOptionValueUpdateRequest(existingVal.getId(), existingVal.getValue(), 1))
                    )),
                    null,
                    List.of(new ProductVariantUpdateRequest(variant.getId(), variant.getTitle(), variant.getSku(), variant.getPrice(), 1, null, null))
            );

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.options", hasSize(1)));

            ProductOptionCombination combo = optionCombinationRepository.findByProductIdOrderByPositionAsc(seededProduct.getId()).getFirst();
            assertThat(combo.getPosition()).isEqualTo(2);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("OD3 - Add new option -> new combination + values inserted")
        void od3_addNewOption() throws Exception {
            ProductOption sizeOption = fixture.createProductOption("Size");
            ProductVariant variant = getSeededVariant();
            ProductOptionValue existingVal = getSeededOptionValue();

            ProductUpdateRequest request = new ProductUpdateRequest(
                    "Add Opt Prod", "Desc", "initial-product", null, null, null,
                    testCategory.getId(), testBrand.getId(),
                    null,
                    List.of(
                            new ProductOptionCombinationUpdateRequest(
                                    testOption.getId(), 1,
                                    List.of(new ProductOptionValueUpdateRequest(existingVal.getId(), existingVal.getValue(), 1))
                            ),
                            new ProductOptionCombinationUpdateRequest(
                                    sizeOption.getId(), 2,
                                    List.of(new ProductOptionValueUpdateRequest(null, "XL", 1))
                            )
                    ),
                    null,
                    List.of(new ProductVariantUpdateRequest(variant.getId(), variant.getTitle(), variant.getSku(), variant.getPrice(), 1, null, null))
            );

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.options", hasSize(2)));

            assertThat(optionCombinationRepository.findByProductIdOrderByPositionAsc(seededProduct.getId())).hasSize(2);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("OD4 - Remove one option -> omitted combination + its values deleted")
        void od4_removeOneOption() throws Exception {
            ProductOption sizeOption = fixture.createProductOption("Size");
            ProductOptionCombination combo2 = fixture.createOptionCombination(seededProduct, sizeOption, 2);
            fixture.createOptionValue(combo2, "M", 1);

            ProductVariant variant = getSeededVariant();
            ProductOptionValue existingVal = getSeededOptionValue();

            ProductUpdateRequest request = new ProductUpdateRequest(
                    "Remove Opt Prod", "Desc", "initial-product", null, null, null,
                    testCategory.getId(), testBrand.getId(),
                    null,
                    List.of(new ProductOptionCombinationUpdateRequest(
                            testOption.getId(), 1,
                            List.of(new ProductOptionValueUpdateRequest(existingVal.getId(), existingVal.getValue(), 1))
                    )),
                    null,
                    List.of(new ProductVariantUpdateRequest(variant.getId(), variant.getTitle(), variant.getSku(), variant.getPrice(), 1, null, null))
            );

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.options", hasSize(1)))
                    .andExpect(jsonPath("$.data.options[0].product_option_id").value(testOption.getId()));

            assertThat(optionCombinationRepository.findByProductIdOrderByPositionAsc(seededProduct.getId())).hasSize(1);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("OD5 - Update a value's text (with id) -> existing value updated in-place")
        void od5_updateValueInPlace() throws Exception {
            ProductVariant variant = getSeededVariant();
            ProductOptionValue existingVal = getSeededOptionValue();

            ProductUpdateRequest request = new ProductUpdateRequest(
                    "Update Val Prod", "Desc", "initial-product", null, null, null,
                    testCategory.getId(), testBrand.getId(),
                    null,
                    List.of(new ProductOptionCombinationUpdateRequest(
                            testOption.getId(), 1,
                            List.of(new ProductOptionValueUpdateRequest(existingVal.getId(), "Crimson Red", 1))
                    )),
                    null,
                    List.of(new ProductVariantUpdateRequest(variant.getId(), variant.getTitle(), variant.getSku(), variant.getPrice(), 1, null, null))
            );

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.options[0].values[0].value").value("Crimson Red"));

            ProductOptionValue updated = optionValueRepository.findById(existingVal.getId()).orElseThrow();
            assertThat(updated.getValue()).isEqualTo("Crimson Red");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("OD6 - Add new value to existing option (no id) -> new ProductOptionValue inserted")
        void od6_addNewValueToOption() throws Exception {
            ProductVariant variant = getSeededVariant();
            ProductOptionValue existingVal = getSeededOptionValue();

            ProductUpdateRequest request = new ProductUpdateRequest(
                    "New Val Prod", "Desc", "initial-product", null, null, null,
                    testCategory.getId(), testBrand.getId(),
                    null,
                    List.of(new ProductOptionCombinationUpdateRequest(
                            testOption.getId(), 1,
                            List.of(
                                    new ProductOptionValueUpdateRequest(existingVal.getId(), existingVal.getValue(), 1),
                                    new ProductOptionValueUpdateRequest(null, "Navy Blue", 2)
                            )
                    )),
                    null,
                    List.of(new ProductVariantUpdateRequest(variant.getId(), variant.getTitle(), variant.getSku(), variant.getPrice(), 1, null, null))
            );

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.options[0].values", hasSize(2)));

            assertThat(optionValueRepository.findByProductId(seededProduct.getId())).hasSize(2);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("OD7 - Remove a value (not sent in request) -> omitted value deleted")
        void od7_removeOmittedValue() throws Exception {
            ProductOptionCombination combo = getSeededCombination();
            ProductOptionValue val2 = fixture.createOptionValue(combo, "Green", 2);

            ProductVariant variant = getSeededVariant();

            ProductUpdateRequest request = new ProductUpdateRequest(
                    "Remove Val Prod", "Desc", "initial-product", null, null, null,
                    testCategory.getId(), testBrand.getId(),
                    null,
                    List.of(new ProductOptionCombinationUpdateRequest(
                            testOption.getId(), 1,
                            List.of(new ProductOptionValueUpdateRequest(val2.getId(), val2.getValue(), 1))
                    )),
                    null,
                    List.of(new ProductVariantUpdateRequest(variant.getId(), variant.getTitle(), variant.getSku(), variant.getPrice(), 1, null, null))
            );

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.options[0].values", hasSize(1)))
                    .andExpect(jsonPath("$.data.options[0].values[0].value").value("Green"));

            List<ProductOptionValue> values = optionValueRepository.findByProductId(seededProduct.getId());
            assertThat(values).hasSize(1);
            assertThat(values.getFirst().getValue()).isEqualTo("Green");
        }
    }

    @Nested
    @DisplayName("2.5 Attributes (Delta)")
    class AttributeDelta {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("AD1 - attributes = null or empty -> all attribute values deleted")
        void ad1_deleteAllAttributes() throws Exception {
            ProductUpdateRequest request = defaultUpdateRequest("No Attr Prod", "initial-product");

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.attributes").isEmpty());

            assertThat(attributeValueRepository.findByProductId(seededProduct.getId())).isEmpty();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("AD2/AD5 - Same attributes re-sent -> value updated in-place")
        void ad2_updateAttributeValue() throws Exception {
            ProductVariant variant = getSeededVariant();
            ProductUpdateRequest request = new ProductUpdateRequest(
                    "Update Attr Prod", "Desc", "initial-product", null, null, null,
                    testCategory.getId(), testBrand.getId(),
                    null, null,
                    List.of(new ProductAttributeValueUpdateRequest(testAttribute.getId(), "2 Years Extended")),
                    List.of(new ProductVariantUpdateRequest(variant.getId(), variant.getTitle(), variant.getSku(), variant.getPrice(), 1, null, null))
            );

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.attributes[0].value").value("2 Years Extended"));

            ProductAttributeValue val = attributeValueRepository.findByProductId(seededProduct.getId()).getFirst();
            assertThat(val.getValue()).isEqualTo("2 Years Extended");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("AD3 - Add new attribute -> inserted")
        void ad3_addNewAttribute() throws Exception {
            ProductAttribute newAttr = fixture.createProductAttribute("Battery");
            ProductVariant variant = getSeededVariant();

            ProductUpdateRequest request = new ProductUpdateRequest(
                    "Add Attr Prod", "Desc", "initial-product", null, null, null,
                    testCategory.getId(), testBrand.getId(),
                    null, null,
                    List.of(
                            new ProductAttributeValueUpdateRequest(testAttribute.getId(), "1 Year"),
                            new ProductAttributeValueUpdateRequest(newAttr.getId(), "5000mAh")
                    ),
                    List.of(new ProductVariantUpdateRequest(variant.getId(), variant.getTitle(), variant.getSku(), variant.getPrice(), 1, null, null))
            );

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.attributes", hasSize(2)));

            assertThat(attributeValueRepository.findByProductId(seededProduct.getId())).hasSize(2);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("AD4 - Remove attribute (omitted from request) -> deleted")
        void ad4_removeAttribute() throws Exception {
            ProductAttribute newAttr = fixture.createProductAttribute("Material");
            attributeValueRepository.save(ProductAttributeValue.builder().product(seededProduct).productAttribute(newAttr).value("Metal").build());

            ProductVariant variant = getSeededVariant();
            ProductUpdateRequest request = new ProductUpdateRequest(
                    "Remove Attr Prod", "Desc", "initial-product", null, null, null,
                    testCategory.getId(), testBrand.getId(),
                    null, null,
                    List.of(new ProductAttributeValueUpdateRequest(newAttr.getId(), "Titanium")),
                    List.of(new ProductVariantUpdateRequest(variant.getId(), variant.getTitle(), variant.getSku(), variant.getPrice(), 1, null, null))
            );

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.attributes", hasSize(1)))
                    .andExpect(jsonPath("$.data.attributes[0].value").value("Titanium"));

            List<ProductAttributeValue> list = attributeValueRepository.findByProductId(seededProduct.getId());
            assertThat(list).hasSize(1);
            assertThat(list.getFirst().getValue()).isEqualTo("Titanium");
        }
    }

    @Nested
    @DisplayName("2.6 Variants (Delta)")
    class VariantDelta {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("VD2 - Same variants (with ID) -> updated in-place via applyVariant")
        void vd2_updateVariantInPlace() throws Exception {
            ProductVariant variant = getSeededVariant();

            ProductUpdateRequest request = new ProductUpdateRequest(
                    "Update Var Prod", "Desc", "initial-product", null, null, null,
                    testCategory.getId(), testBrand.getId(),
                    null, null, null,
                    List.of(new ProductVariantUpdateRequest(
                            variant.getId(),
                            "Updated Title",
                            "UPDATED-SKU",
                            new BigDecimal("199.99"),
                            50,
                            null,
                            null
                    ))
            );

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.variants[0].sku").value("UPDATED-SKU"))
                    .andExpect(jsonPath("$.data.variants[0].title").value("Updated Title"))
                    .andExpect(jsonPath("$.data.variants[0].quantity").value(50));

            ProductVariant updated = productVariantRepository.findById(variant.getId()).orElseThrow();
            assertThat(updated.getSku()).isEqualTo("UPDATED-SKU");
            assertThat(updated.getTitle()).isEqualTo("Updated Title");
            assertThat(updated.getQuantity()).isEqualTo(50);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("VD3 - Add new variant (no id) -> new ProductVariant inserted")
        void vd3_addNewVariant() throws Exception {
            ProductVariant existing = getSeededVariant();

            ProductUpdateRequest request = new ProductUpdateRequest(
                    "Add Var Prod", "Desc", "initial-product", null, null, null,
                    testCategory.getId(), testBrand.getId(),
                    null, null, null,
                    List.of(
                            new ProductVariantUpdateRequest(existing.getId(), existing.getTitle(), existing.getSku(), existing.getPrice(), existing.getQuantity(), null, null),
                            new ProductVariantUpdateRequest(null, "New Variant", "NEW-SKU-VD3", new BigDecimal("79.99"), 15, null, null)
                    )
            );

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.variants", hasSize(2)));

            assertThat(productVariantRepository.findByProductId(seededProduct.getId())).hasSize(2);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("VD4 - Remove variant (omit from request) -> omitted variant deleted")
        void vd4_removeVariant() throws Exception {
            ProductVariant variant2 = productVariantRepository.save(ProductVariant.builder()
                    .product(seededProduct)
                    .sku("VAR-TO-KEEP")
                    .title("Keep Me")
                    .price(new BigDecimal("150.00"))
                    .quantity(5)
                    .build());

            ProductUpdateRequest request = new ProductUpdateRequest(
                    "Remove Var Prod", "Desc", "initial-product", null, null, null,
                    testCategory.getId(), testBrand.getId(),
                    null, null, null,
                    List.of(new ProductVariantUpdateRequest(variant2.getId(), variant2.getTitle(), variant2.getSku(), variant2.getPrice(), variant2.getQuantity(), null, null))
            );

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.variants", hasSize(1)))
                    .andExpect(jsonPath("$.data.variants[0].sku").value("VAR-TO-KEEP"));

            List<ProductVariant> variants = productVariantRepository.findByProductId(seededProduct.getId());
            assertThat(variants).hasSize(1);
            assertThat(variants.getFirst().getSku()).isEqualTo("VAR-TO-KEEP");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("VOV2/VOV3 - VariantOptionValue re-sent preserved, new one added")
        void vov2_vov3_variantOptionValuesDelta() throws Exception {
            ProductVariant variant = getSeededVariant();
            ProductOptionValue optVal1 = getSeededOptionValue();

            ProductOptionCombination combo = getSeededCombination();
            ProductOptionValue optVal2 = fixture.createOptionValue(combo, "Blue", 2);

            ProductUpdateRequest request = new ProductUpdateRequest(
                    "VOV Delta Prod", "Desc", "initial-product", null, null, null,
                    testCategory.getId(), testBrand.getId(),
                    null,
                    List.of(new ProductOptionCombinationUpdateRequest(
                            testOption.getId(), 1,
                            List.of(
                                    new ProductOptionValueUpdateRequest(optVal1.getId(), optVal1.getValue(), 1),
                                    new ProductOptionValueUpdateRequest(optVal2.getId(), optVal2.getValue(), 2)
                            )
                    )),
                    null,
                    List.of(new ProductVariantUpdateRequest(variant.getId(), variant.getTitle(), variant.getSku(), variant.getPrice(), variant.getQuantity(), null, null))
            );

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            List<VariantOptionValue> vovs = variantOptionValueRepository.findByProductVariantProductId(seededProduct.getId());
            assertThat(vovs).isNotEmpty();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("VAV2/VAV5 - ProductVariantAttributeValue updated in-place")
        void vav2_vav5_variantAttributeUpdated() throws Exception {
            ProductVariant variant = getSeededVariant();

            ProductUpdateRequest request = new ProductUpdateRequest(
                    "VAV Update Prod", "Desc", "initial-product", null, null, null,
                    testCategory.getId(), testBrand.getId(),
                    null, null, null,
                    List.of(new ProductVariantUpdateRequest(
                            variant.getId(),
                            variant.getTitle(),
                            variant.getSku(),
                            variant.getPrice(),
                            variant.getQuantity(),
                            null,
                            List.of(new ProductVariantAttributeValueUpdateRequest(testAttribute.getId(), "Updated Spec Value"))
                    ))
            );

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.variants[0].attribute_values[0].value").value("Updated Spec Value"));

            List<ProductVariantAttributeValue> vavs = variantAttributeValueRepository.findByProductVariantId(variant.getId());
            assertThat(vavs).hasSize(1);
            assertThat(vavs.getFirst().getValue()).isEqualTo("Updated Spec Value");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("VAV3 - New attribute added to variant -> inserted")
        void vav3_variantAttributeAdded() throws Exception {
            ProductAttribute newAttr = fixture.createProductAttribute("Speed");
            ProductVariant variant = getSeededVariant();

            ProductUpdateRequest request = new ProductUpdateRequest(
                    "VAV Add Prod", "Desc", "initial-product", null, null, null,
                    testCategory.getId(), testBrand.getId(),
                    null, null, null,
                    List.of(new ProductVariantUpdateRequest(
                            variant.getId(),
                            variant.getTitle(),
                            variant.getSku(),
                            variant.getPrice(),
                            variant.getQuantity(),
                            null,
                            List.of(
                                    new ProductVariantAttributeValueUpdateRequest(testAttribute.getId(), "100% Organic"),
                                    new ProductVariantAttributeValueUpdateRequest(newAttr.getId(), "120Hz")
                            )
                    ))
            );

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.variants[0].attribute_values", hasSize(2)));

            assertThat(variantAttributeValueRepository.findByProductVariantId(variant.getId())).hasSize(2);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("VAV4 - Variant attribute omitted -> deleted")
        void vav4_variantAttributeDeleted() throws Exception {
            ProductVariant variant = getSeededVariant();

            ProductUpdateRequest request = new ProductUpdateRequest(
                    "VAV Delete Prod", "Desc", "initial-product", null, null, null,
                    testCategory.getId(), testBrand.getId(),
                    null, null, null,
                    List.of(new ProductVariantUpdateRequest(
                            variant.getId(),
                            variant.getTitle(),
                            variant.getSku(),
                            variant.getPrice(),
                            variant.getQuantity(),
                            null,
                            List.of()
                    ))
            );

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.variants[0].attribute_values").isEmpty());

            assertThat(variantAttributeValueRepository.findByProductVariantId(variant.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("2.7 Combined (Happy Path)")
    class CombinedHappyPaths {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("H1 - Full product update: name, media, swap option, attribute, variant delta")
        void h1_fullUpdateHappyPath() throws Exception {
            ProductVariant variant = getSeededVariant();
            Media newMedia = fixture.createMedia("h1-media", "https://media.example.com/h1.jpg");
            ProductOption newOption = fixture.createProductOption("Storage");
            ProductAttribute newAttr = fixture.createProductAttribute("Water Resistance");

            ProductUpdateRequest request = new ProductUpdateRequest(
                    "Fully Updated Name",
                    "New detailed description",
                    "fully-updated-name",
                    "New Meta Title",
                    "new, tags",
                    "New Meta Desc",
                    testCategory.getId(),
                    testBrand.getId(),
                    List.of(new ProductMediaRequest(newMedia.getId().toString(), 1)),
                    List.of(new ProductOptionCombinationUpdateRequest(
                            newOption.getId(), 1,
                            List.of(new ProductOptionValueUpdateRequest(null, "512GB", 1))
                    )),
                    List.of(new ProductAttributeValueUpdateRequest(newAttr.getId(), "IP68")),
                    List.of(new ProductVariantUpdateRequest(
                            variant.getId(),
                            "Pro 512GB",
                            "PRO-512-NEW",
                            new BigDecimal("1299.99"),
                            100,
                            newMedia.getId().toString(),
                            List.of(new ProductVariantAttributeValueUpdateRequest(newAttr.getId(), "Tested to 6m"))
                    ))
            );

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("200"))
                    .andExpect(jsonPath("$.data.name").value("Fully Updated Name"))
                    .andExpect(jsonPath("$.data.slug").value("fully-updated-name"))
                    .andExpect(jsonPath("$.data.medias", hasSize(1)))
                    .andExpect(jsonPath("$.data.options", hasSize(1)))
                    .andExpect(jsonPath("$.data.attributes", hasSize(1)))
                    .andExpect(jsonPath("$.data.variants", hasSize(1)))
                    .andExpect(jsonPath("$.data.variants[0].sku").value("PRO-512-NEW"));

            Product updated = productRepository.findById(seededProduct.getId()).orElseThrow();
            assertThat(updated.getName()).isEqualTo("Fully Updated Name");
            assertThat(productMediaRepository.findByProductIdOrderByPositionAsc(seededProduct.getId())).hasSize(1);
            assertThat(optionCombinationRepository.findByProductIdOrderByPositionAsc(seededProduct.getId())).hasSize(1);
            assertThat(attributeValueRepository.findByProductId(seededProduct.getId())).hasSize(1);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("H2 - Update only base fields, keep all options/attributes/variants unchanged")
        void h2_updateOnlyBaseFields() throws Exception {
            ProductVariant variant = getSeededVariant();
            ProductOptionValue optVal = getSeededOptionValue();

            ProductUpdateRequest request = new ProductUpdateRequest(
                    "Only Base Fields Changed",
                    "New description only",
                    "only-base-fields-changed",
                    "New Meta Title",
                    "new, keywords",
                    "New Meta Desc",
                    testCategory.getId(),
                    testBrand.getId(),
                    List.of(new ProductMediaRequest(testMedia.getId().toString(), 1)),
                    List.of(new ProductOptionCombinationUpdateRequest(
                            testOption.getId(), 1,
                            List.of(new ProductOptionValueUpdateRequest(optVal.getId(), optVal.getValue(), 1))
                    )),
                    List.of(new ProductAttributeValueUpdateRequest(testAttribute.getId(), "Cotton")),
                    List.of(new ProductVariantUpdateRequest(
                            variant.getId(),
                            variant.getTitle(),
                            variant.getSku(),
                            variant.getPrice(),
                            variant.getQuantity(),
                            variant.getMediaId(),
                            List.of(new ProductVariantAttributeValueUpdateRequest(testAttribute.getId(), "100% Organic"))
                    ))
            );

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("Only Base Fields Changed"))
                    .andExpect(jsonPath("$.data.slug").value("only-base-fields-changed"));

            Product updated = productRepository.findById(seededProduct.getId()).orElseThrow();
            assertThat(updated.getName()).isEqualTo("Only Base Fields Changed");
            assertThat(productVariantRepository.findByProductId(seededProduct.getId())).hasSize(1);
        }
    }

    @Nested
    @DisplayName("3. Option Combination Deletion Order (FK Safety)")
    class FkOrderingSafety {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("FK1 - Remove option still referenced by VariantOptionValue -> NO FK constraint violation")
        void fk1_removeReferencedOption_noFkViolation() throws Exception {
            ProductVariant variant = getSeededVariant();

            ProductUpdateRequest request = new ProductUpdateRequest(
                    "FK Safe Product",
                    "Desc",
                    "fk-safe-product",
                    null, null, null,
                    testCategory.getId(),
                    testBrand.getId(),
                    null,
                    null,
                    null,
                    List.of(new ProductVariantUpdateRequest(
                            variant.getId(),
                            variant.getTitle(),
                            variant.getSku(),
                            variant.getPrice(),
                            variant.getQuantity(),
                            null,
                            null
                    ))
            );

            mockMvc.perform(put("/api/v1/products/{id}", seededProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("200"))
                    .andExpect(jsonPath("$.data.options").isEmpty());

            assertThat(optionCombinationRepository.findByProductIdOrderByPositionAsc(seededProduct.getId())).isEmpty();
            assertThat(optionValueRepository.findByProductId(seededProduct.getId())).isEmpty();
            assertThat(variantOptionValueRepository.findByProductVariantProductId(seededProduct.getId())).isEmpty();
        }
    }
}
