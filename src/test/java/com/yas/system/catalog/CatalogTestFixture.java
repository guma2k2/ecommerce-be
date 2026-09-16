package com.yas.system.catalog;

import com.yas.system.catalog.internal.entity.Brand;
import com.yas.system.catalog.internal.entity.Category;
import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.entity.ProductMedia;
import com.yas.system.catalog.internal.entity.attribute.ProductAttribute;
import com.yas.system.catalog.internal.entity.attribute.ProductAttributeValue;
import com.yas.system.catalog.internal.entity.attribute.ProductVariantAttributeValue;
import com.yas.system.catalog.internal.entity.option.ProductOption;
import com.yas.system.catalog.internal.entity.option.ProductOptionCombination;
import com.yas.system.catalog.internal.entity.option.ProductOptionCombinationId;
import com.yas.system.catalog.internal.entity.option.ProductOptionValue;
import com.yas.system.catalog.internal.entity.variant.ProductVariant;
import com.yas.system.catalog.internal.entity.variant.VariantOptionValue;
import com.yas.system.catalog.internal.repository.*;
import com.yas.system.media.internal.entity.Media;
import com.yas.system.media.internal.enums.MediaType;
import com.yas.system.media.internal.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CatalogTestFixture {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductVariantAttributeValueRepository productVariantAttributeValueRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductOptionCombinationRepository productOptionCombinationRepository;
    private final ProductOptionValueRepository productOptionValueRepository;
    private final VariantOptionValueRepository variantOptionValueRepository;
    private final ProductAttributeRepository productAttributeRepository;
    private final ProductAttributeValueRepository productAttributeValueRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductMediaRepository productMediaRepository;
    private final MediaRepository mediaRepository;

    @Transactional
    public void cleanDatabase() {
        productVariantAttributeValueRepository.deleteAllInBatch();
        variantOptionValueRepository.deleteAllInBatch();
        productVariantRepository.deleteAllInBatch();
        productOptionValueRepository.deleteAllInBatch();
        productOptionCombinationRepository.deleteAllInBatch();
        productAttributeValueRepository.deleteAllInBatch();
        productMediaRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        brandRepository.deleteAllInBatch();
        productOptionRepository.deleteAllInBatch();
        productAttributeRepository.deleteAllInBatch();
        mediaRepository.deleteAllInBatch();
    }

    public Category createCategory(String name) {
        return categoryRepository.save(Category.builder().name(name).build());
    }

    public Brand createBrand(String name) {
        return brandRepository.save(Brand.builder().name(name).description("Brand " + name).build());
    }

    public ProductOption createProductOption(String name) {
        return productOptionRepository.save(ProductOption.builder().name(name).build());
    }

    public ProductAttribute createProductAttribute(String name) {
        return productAttributeRepository.save(ProductAttribute.builder().name(name).build());
    }

    public Media createMedia(String name, String url) {
        return mediaRepository.save(Media.builder()
                .name(name)
                .url(url)
                .active(true)
                .size(1024)
                .type(MediaType.IMAGE)
                .fileType("image/jpeg")
                .build());
    }

    public Product findProductBySlug(String slug) {
        return productRepository.findAll().stream()
                .filter(p -> slug.equals(p.getSlug()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Product not found with slug: " + slug));
    }

    public ProductOptionCombination createOptionCombination(Product product, ProductOption option, int position) {
        return productOptionCombinationRepository.save(
                ProductOptionCombination.builder()
                        .id(new ProductOptionCombinationId(product.getId(), option.getId()))
                        .product(product)
                        .productOption(option)
                        .position(position)
                        .build());
    }

    public ProductOptionValue createOptionValue(ProductOptionCombination combination, String value, int position) {
        return productOptionValueRepository.save(
                ProductOptionValue.builder()
                        .productOptionCombination(combination)
                        .value(value)
                        .position(position)
                        .build());
    }

    @Transactional
    public Product createSeedProductWithFullRelations(
            String name,
            String slug,
            Category category,
            Brand brand,
            Media media,
            ProductOption option,
            ProductAttribute attribute
    ) {
        // Base product
        Product product = productRepository.save(Product.builder()
                .name(name)
                .slug(slug)
                .description("Initial description")
                .metaTitle("Initial meta title")
                .metaKeyword("seed, product")
                .metaDescription("Initial meta description")
                .category(category)
                .brand(brand)
                .build());

        // Product Media
        productMediaRepository.save(ProductMedia.builder()
                .product(product)
                .mediaId(media.getId().toString())
                .position(1)
                .build());

        // Product Option Combination + Option Value
        ProductOptionCombination combination = createOptionCombination(product, option, 1);

        ProductOptionValue optionValue = createOptionValue(combination, "Red", 1);

        // Product Attribute Value
        productAttributeValueRepository.save(
                ProductAttributeValue.builder()
                        .product(product)
                        .productAttribute(attribute)
                        .value("Cotton")
                        .build());

        // Product Variant
        ProductVariant variant = productVariantRepository.save(
                ProductVariant.builder()
                        .product(product)
                        .sku(slug + "-VAR-1")
                        .title("Variant 1")
                        .price(new BigDecimal("99.99"))
                        .quantity(10)
                        .mediaId(media.getId().toString())
                        .build());

        // Variant Option Value
        variantOptionValueRepository.save(
                VariantOptionValue.builder()
                        .productVariant(variant)
                        .productOptionValue(optionValue)
                        .build());

        // Variant Attribute Value
        productVariantAttributeValueRepository.save(
                ProductVariantAttributeValue.builder()
                        .productVariant(variant)
                        .productAttribute(attribute)
                        .value("100% Organic")
                        .build());

        return product;
    }
}
