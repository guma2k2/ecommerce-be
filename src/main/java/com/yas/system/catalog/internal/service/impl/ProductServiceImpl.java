package com.yas.system.catalog.internal.service.impl;

import com.yas.system.catalog.internal.dto.request.ProductAttributeValueCreateRequest;
import com.yas.system.catalog.internal.dto.request.ProductAttributeValueUpdateRequest;
import com.yas.system.catalog.internal.dto.request.ProductCreateRequest;
import com.yas.system.catalog.internal.dto.request.ProductOptionCreateRequest;
import com.yas.system.catalog.internal.dto.request.ProductOptionUpdateRequest;
import com.yas.system.catalog.internal.dto.request.ProductUpdateRequest;
import com.yas.system.catalog.internal.dto.request.ProductVariantCreateRequest;
import com.yas.system.catalog.internal.dto.request.ProductVariantUpdateRequest;
import com.yas.system.catalog.internal.dto.response.ProductResponse;
import com.yas.system.catalog.internal.entity.Brand;
import com.yas.system.catalog.internal.entity.Category;
import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.entity.ProductCategory;
import com.yas.system.catalog.internal.entity.ProductCategoryId;
import com.yas.system.catalog.internal.entity.ProductOption;
import com.yas.system.catalog.internal.entity.ProductVariant;
import com.yas.system.catalog.internal.entity.VariantOptionValue;
import com.yas.system.catalog.internal.entity.attribute.ProductAttribute;
import com.yas.system.catalog.internal.entity.attribute.ProductAttributeValue;
import com.yas.system.catalog.internal.helper.ProductHelper;
import com.yas.system.catalog.internal.repository.BrandRepository;
import com.yas.system.catalog.internal.repository.CategoryRepository;
import com.yas.system.catalog.internal.repository.ProductAttributeRepository;
import com.yas.system.catalog.internal.repository.ProductAttributeValueRepository;
import com.yas.system.catalog.internal.repository.ProductCategoryRepository;
import com.yas.system.catalog.internal.repository.ProductOptionRepository;
import com.yas.system.catalog.internal.repository.ProductRepository;
import com.yas.system.catalog.internal.repository.ProductVariantRepository;
import com.yas.system.catalog.internal.repository.VariantOptionValueRepository;
import com.yas.system.catalog.internal.service.ProductService;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.exception.InvalidDataException;
import com.yas.system.common.exception.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.yas.system.common.util.StringUtils.isBlank;
import static com.yas.system.common.constant.AppConstant.PRODUCT_VARIANT_DEFAULT_TITLE;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductServiceImpl implements ProductService {

    ProductRepository productRepository;
    ProductVariantRepository productVariantRepository;
    ProductOptionRepository productOptionRepository;
    VariantOptionValueRepository variantOptionValueRepository;
    ProductAttributeRepository productAttributeRepository;
    ProductAttributeValueRepository productAttributeValueRepository;
    CategoryRepository categoryRepository;
    BrandRepository brandRepository;
    ProductCategoryRepository productCategoryRepository;
    ProductHelper productHelper;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        validateProductRequest(request);

        Category category = resolveCategory(request.categoryId());
        Brand brand = resolveBrand(request.brandId());
        Product savedProduct = productRepository.save(productHelper.createProduct(request, brand));

        List<Category> categoryHierarchy = collectCategoryAndParents(category);
        saveProductCategories(savedProduct, categoryHierarchy);

        List<ProductAttributeValue> savedAttributes = saveProductAttributeValues(request, savedProduct);
        List<ProductOption> savedOptions = saveOptions(request, savedProduct);
        List<ProductVariant> savedVariants = saveVariants(request, savedProduct);
        List<VariantOptionValue> savedVariantOptionValues = saveVariantOptionValuesForCreate(
                request,
                savedOptions,
                savedVariants
        );

        return ProductResponse.from(savedProduct, savedAttributes, savedOptions, savedVariants, savedVariantOptionValues);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(ProductUpdateRequest request, Long productId) {
        validateProductRequest(request, productId);

        Category category = resolveCategory(request.categoryId());
        Brand brand = resolveBrand(request.brandId());
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        productHelper.updateProduct(request, product, brand);
        Product savedProduct = productRepository.save(product);

        productCategoryRepository.deleteByProductId(productId);
        List<Category> categoryHierarchy = collectCategoryAndParents(category);
        saveProductCategories(savedProduct, categoryHierarchy);

        List<ProductAttributeValue> currentAttributes = productAttributeValueRepository.findByProductId(productId);
        List<ProductOption> currentOptions = productOptionRepository.findByProductId(productId);
        List<ProductVariant> currentVariants = productVariantRepository.findByProductId(productId);
        variantOptionValueRepository.deleteByProductId(productId);

        List<ProductAttributeValue> savedAttributes = saveUpdatedProductAttributeValues(
                request,
                savedProduct,
                currentAttributes
        );
        List<ProductOption> savedOptions = saveUpdatedOptions(request, savedProduct, currentOptions);
        List<ProductVariant> savedVariants = saveUpdatedVariants(request, savedProduct, currentVariants);
        List<VariantOptionValue> savedVariantOptionValues = saveVariantOptionValuesForUpdate(
                request,
                savedOptions,
                savedVariants
        );

        return ProductResponse.from(savedProduct, savedAttributes, savedOptions, savedVariants, savedVariantOptionValues);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        if (Objects.isNull(id)) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        List<ProductAttributeValue> attributes = productAttributeValueRepository.findByProductId(id);
        List<ProductOption> options = productOptionRepository.findByProductId(id);
        List<ProductVariant> variants = productVariantRepository.findByProductId(id);
        List<VariantOptionValue> variantOptionValues = variantOptionValueRepository.findByProductVariantProductId(id);

        return ProductResponse.from(product, attributes, options, variants, variantOptionValues);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (Objects.isNull(id)) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        productCategoryRepository.deleteByProductId(id);
        productAttributeValueRepository.deleteByProductId(id);
        variantOptionValueRepository.deleteByProductId(id);
        productOptionRepository.deleteByProductId(id);
        productVariantRepository.deleteByProductId(id);
        productRepository.delete(product);
    }

    private Category resolveCategory(Integer categoryId) {
        if (Objects.isNull(categoryId)) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private Brand resolveBrand(Long brandId) {
        if (Objects.isNull(brandId)) {
            return null;
        }
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BRAND_NOT_FOUND));
    }

    private List<Category> collectCategoryAndParents(Category category) {
        if (Objects.isNull(category)) {
            return List.of();
        }
        List<Category> categories = new ArrayList<>();
        Category current = category;
        Set<Integer> visitedIds = new HashSet<>();
        while (Objects.nonNull(current) && !visitedIds.contains(current.getId())) {
            categories.add(current);
            visitedIds.add(current.getId());
            current = current.getParent();
        }
        return categories;
    }

    private List<ProductCategory> saveProductCategories(Product product, List<Category> categories) {
        if (Objects.isNull(categories) || categories.isEmpty()) {
            return List.of();
        }
        List<ProductCategory> productCategories = categories.stream()
                .map(category -> ProductCategory.builder()
                        .id(new ProductCategoryId(product.getId(), category.getId()))
                        .product(product)
                        .category(category)
                        .build())
                .toList();
        return productCategoryRepository.saveAll(productCategories);
    }

    private List<ProductAttributeValue> saveProductAttributeValues(ProductCreateRequest request, Product product) {
        if (Objects.isNull(request.attributes()) || request.attributes().isEmpty()) {
            return List.of();
        }
        List<Long> productAttributeIds = request.attributes().stream()
                .map(ProductAttributeValueCreateRequest::productAttributeId)
                .toList();
        Map<Long, ProductAttribute> productAttributeById = findProductAttributes(productAttributeIds);
        List<ProductAttributeValue> attributeValues = request.attributes().stream()
                .map(attributeRequest -> ProductAttributeValue.builder()
                        .product(product)
                        .productAttribute(productAttributeById.get(attributeRequest.productAttributeId()))
                        .value(attributeRequest.value())
                        .build())
                .toList();
        return productAttributeValueRepository.saveAll(attributeValues);
    }

    private List<ProductAttributeValue> saveUpdatedProductAttributeValues(
            ProductUpdateRequest request,
            Product product,
            List<ProductAttributeValue> currentAttributes
    ) {
        if (Objects.isNull(request.attributes()) || request.attributes().isEmpty()) {
            productAttributeValueRepository.deleteAll(currentAttributes);
            return List.of();
        }
        List<Long> productAttributeIds = request.attributes().stream()
                .map(ProductAttributeValueUpdateRequest::productAttributeId)
                .toList();
        Map<Long, ProductAttribute> productAttributeById = findProductAttributes(productAttributeIds);
        Map<Long, ProductAttributeValue> currentAttributeByProductAttributeId = currentAttributes.stream()
                .collect(Collectors.toMap(
                        attributeValue -> attributeValue.getProductAttribute().getId(),
                        Function.identity()
                ));
        List<ProductAttributeValue> attributeValues = request.attributes().stream()
                .map(attributeRequest -> resolveProductAttributeValue(
                        attributeRequest,
                        product,
                        productAttributeById,
                        currentAttributeByProductAttributeId
                ))
                .toList();
        deleteMissingProductAttributeValues(request, currentAttributes);
        return productAttributeValueRepository.saveAll(attributeValues);
    }

    private Map<Long, ProductAttribute> findProductAttributes(List<Long> productAttributeIds) {
        Map<Long, ProductAttribute> productAttributeById = productAttributeRepository.findAllById(productAttributeIds)
                .stream()
                .collect(Collectors.toMap(ProductAttribute::getId, Function.identity()));
        if (productAttributeById.size() != productAttributeIds.stream().distinct().count()) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_ATTRIBUTE_NOT_FOUND);
        }
        return productAttributeById;
    }

    private ProductAttributeValue resolveProductAttributeValue(
            ProductAttributeValueUpdateRequest request,
            Product product,
            Map<Long, ProductAttribute> productAttributeById,
            Map<Long, ProductAttributeValue> currentAttributeByProductAttributeId
    ) {
        ProductAttributeValue attributeValue = currentAttributeByProductAttributeId.get(request.productAttributeId());
        if (Objects.isNull(attributeValue)) {
            return ProductAttributeValue.builder()
                    .product(product)
                    .productAttribute(productAttributeById.get(request.productAttributeId()))
                    .value(request.value())
                    .build();
        }
        attributeValue.setValue(request.value());
        return attributeValue;
    }

    private void deleteMissingProductAttributeValues(
            ProductUpdateRequest request,
            List<ProductAttributeValue> currentAttributes
    ) {
        List<Long> requestProductAttributeIds = request.attributes().stream()
                .map(ProductAttributeValueUpdateRequest::productAttributeId)
                .toList();
        List<ProductAttributeValue> deletedAttributes = currentAttributes.stream()
                .filter(attributeValue -> !requestProductAttributeIds.contains(attributeValue.getProductAttribute().getId()))
                .toList();
        productAttributeValueRepository.deleteAll(deletedAttributes);
    }

    private List<ProductOption> saveOptions(ProductCreateRequest request, Product product) {
        List<ProductOption> options = request.options().stream()
                .map(optionRequest -> optionRequest.toEntity(product))
                .toList();
        return productOptionRepository.saveAll(options);
    }

    private List<ProductVariant> saveVariants(ProductCreateRequest request, Product product) {
        boolean hasOptions = hasOptions(request.options());
        return request.variants().stream()
                .map(variantRequest -> {
                    ProductVariant variant = variantRequest.toEntity(product);
                    applyDefaultVariantTitle(variant, hasOptions);
                    return productVariantRepository.save(variant);
                })
                .toList();
    }

    private List<VariantOptionValue> saveVariantOptionValuesForCreate(
            ProductCreateRequest request,
            List<ProductOption> options,
            List<ProductVariant> variants
    ) {
        if (!hasOptions(request.options())) {
            return List.of();
        }
        Map<Integer, ProductOption> optionByPosition = options.stream()
                .collect(Collectors.toMap(ProductOption::getPosition, Function.identity()));
        List<VariantOptionValue> variantOptionValues = buildVariantOptionValuesForCreate(
                request.variants(),
                variants,
                optionByPosition
        );
        return variantOptionValueRepository.saveAll(variantOptionValues);
    }

    private List<VariantOptionValue> saveVariantOptionValuesForUpdate(
            ProductUpdateRequest request,
            List<ProductOption> options,
            List<ProductVariant> variants
    ) {
        if (!hasOptions(request.options())) {
            return List.of();
        }
        Map<Integer, ProductOption> optionByPosition = options.stream()
                .collect(Collectors.toMap(ProductOption::getPosition, Function.identity()));
        List<VariantOptionValue> variantOptionValues = buildVariantOptionValuesForUpdate(
                request.variants(),
                variants,
                optionByPosition
        );
        return variantOptionValueRepository.saveAll(variantOptionValues);
    }

    private void validateProductRequest(ProductCreateRequest request) {
        if (productRepository.existsByName(request.name()) || productRepository.existsBySlug(request.slug())) {
            throw new InvalidDataException(ErrorCode.PRODUCT_ALREADY_EXISTS);
        }
        validateCreateAttributes(request.attributes());
        validateCreateOptions(request.options());
        validateCreateVariants(request.variants());
    }

    private void validateProductRequest(ProductUpdateRequest request, Long productId) {
        if (Objects.isNull(productId)) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
        if (productRepository.existsByNameAndIdNot(request.name(), productId)
                || productRepository.existsBySlugAndIdNot(request.slug(), productId)) {
            throw new InvalidDataException(ErrorCode.PRODUCT_ALREADY_EXISTS);
        }
        validateUpdateAttributes(request.attributes());
        validateUpdateOptions(request.options());
        validateUpdateVariants(request.variants());
        validateUpdateIds(request);
    }

    private void validateUpdateIds(ProductUpdateRequest request) {
        boolean hasDuplicateOptionId = hasDuplicateIds(request.options().stream()
                .map(ProductOptionUpdateRequest::id)
                .filter(Objects::nonNull)
                .toList());
        boolean hasDuplicateVariantId = hasDuplicateIds(request.variants().stream()
                .map(ProductVariantUpdateRequest::id)
                .filter(Objects::nonNull)
                .toList());
        if (hasDuplicateOptionId || hasDuplicateVariantId) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
    }

    private boolean hasDuplicateIds(List<Long> ids) {
        return ids.stream().distinct().count() != ids.size();
    }

    private void validateCreateOptions(List<ProductOptionCreateRequest> options) {
        if (Objects.isNull(options)) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
        if (options.isEmpty()) {
            return;
        }
        boolean invalidOption = options.stream()
                .anyMatch(option -> Objects.isNull(option)
                        || isBlank(option.name())
                        || Objects.isNull(option.values())
                        || option.values().isEmpty()
                        || option.position() < 1
                        || option.position() > 3
                        || option.values().stream().anyMatch(value -> isBlank(value)));
        if (invalidOption || hasDuplicatePositions(options.stream().map(ProductOptionCreateRequest::position).toList())) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
    }

    private void validateUpdateOptions(List<ProductOptionUpdateRequest> options) {
        if (Objects.isNull(options)) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
        if (options.isEmpty()) {
            return;
        }
        boolean invalidOption = options.stream()
                .anyMatch(option -> Objects.isNull(option)
                        || isBlank(option.name())
                        || Objects.isNull(option.values())
                        || option.values().isEmpty()
                        || option.position() < 1
                        || option.position() > 3
                        || option.values().stream().anyMatch(value -> isBlank(value)));
        if (invalidOption || hasDuplicatePositions(options.stream().map(ProductOptionUpdateRequest::position).toList())) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
    }

    private void validateCreateVariants(List<ProductVariantCreateRequest> variants) {
        if (Objects.isNull(variants) || variants.isEmpty()) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
        boolean invalidVariant = variants.stream()
                .anyMatch(variant -> Objects.isNull(variant)
                        || isBlank(variant.sku())
                        || Objects.isNull(variant.price())
                        || variant.price().compareTo(BigDecimal.ZERO) < 0
                        || variant.quantity() < 0);
        if (invalidVariant) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
    }

    private void validateUpdateVariants(List<ProductVariantUpdateRequest> variants) {
        if (Objects.isNull(variants) || variants.isEmpty()) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
        boolean invalidVariant = variants.stream()
                .anyMatch(variant -> Objects.isNull(variant)
                        || isBlank(variant.sku())
                        || Objects.isNull(variant.price())
                        || variant.price().compareTo(BigDecimal.ZERO) < 0
                        || variant.quantity() < 0);
        if (invalidVariant) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
    }

    private void validateCreateAttributes(List<ProductAttributeValueCreateRequest> attributes) {
        if (Objects.isNull(attributes) || attributes.isEmpty()) {
            return;
        }
        boolean invalidAttribute = attributes.stream()
                .anyMatch(attribute -> Objects.isNull(attribute)
                        || Objects.isNull(attribute.productAttributeId())
                        || isBlank(attribute.value()));
        if (invalidAttribute) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
        boolean hasDuplicateAttribute = attributes.stream()
                .map(ProductAttributeValueCreateRequest::productAttributeId)
                .distinct()
                .count() != attributes.size();
        if (hasDuplicateAttribute) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
    }

    private void validateUpdateAttributes(List<ProductAttributeValueUpdateRequest> attributes) {
        if (Objects.isNull(attributes) || attributes.isEmpty()) {
            return;
        }
        boolean invalidAttribute = attributes.stream()
                .anyMatch(attribute -> Objects.isNull(attribute)
                        || Objects.isNull(attribute.productAttributeId())
                        || isBlank(attribute.value()));
        if (invalidAttribute) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
        boolean hasDuplicateAttribute = attributes.stream()
                .map(ProductAttributeValueUpdateRequest::productAttributeId)
                .distinct()
                .count() != attributes.size();
        if (hasDuplicateAttribute) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
    }

    private boolean hasDuplicatePositions(List<Integer> positions) {
        return positions.stream()
                .distinct()
                .count() != positions.size();
    }

    private List<VariantOptionValue> buildVariantOptionValuesForCreate(
            List<ProductVariantCreateRequest> variantRequests,
            List<ProductVariant> savedVariants,
            Map<Integer, ProductOption> optionByPosition
    ) {
        return IntStream.range(0, variantRequests.size())
                .boxed()
                .flatMap(index -> buildVariantOptionValues(
                        variantRequests.get(index).option1(),
                        variantRequests.get(index).option2(),
                        variantRequests.get(index).option3(),
                        savedVariants.get(index),
                        optionByPosition
                ).stream())
                .toList();
    }

    private List<VariantOptionValue> buildVariantOptionValuesForUpdate(
            List<ProductVariantUpdateRequest> variantRequests,
            List<ProductVariant> savedVariants,
            Map<Integer, ProductOption> optionByPosition
    ) {
        return IntStream.range(0, variantRequests.size())
                .boxed()
                .flatMap(index -> buildVariantOptionValues(
                        variantRequests.get(index).option1(),
                        variantRequests.get(index).option2(),
                        variantRequests.get(index).option3(),
                        savedVariants.get(index),
                        optionByPosition
                ).stream())
                .toList();
    }

    private List<VariantOptionValue> buildVariantOptionValues(
            String option1,
            String option2,
            String option3,
            ProductVariant variant,
            Map<Integer, ProductOption> optionByPosition
    ) {
        return Stream.of(
                        buildVariantOptionValue(option1, variant, optionByPosition.get(1)),
                        buildVariantOptionValue(option2, variant, optionByPosition.get(2)),
                        buildVariantOptionValue(option3, variant, optionByPosition.get(3))
                )
                .filter(Objects::nonNull)
                .toList();
    }

    private VariantOptionValue buildVariantOptionValue(String value, ProductVariant variant, ProductOption option) {
        if (isBlank(value)) {
            return null;
        }
        if (Objects.isNull(option) || !option.getValues().contains(value)) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
        return VariantOptionValue.builder()
                .value(value)
                .productVariant(variant)
                .productOption(option)
                .build();
    }

    private List<ProductOption> saveUpdatedOptions(
            ProductUpdateRequest request,
            Product product,
            List<ProductOption> currentOptions
    ) {
        Map<Long, ProductOption> currentOptionById = currentOptions.stream()
                .collect(Collectors.toMap(ProductOption::getId, Function.identity()));
        List<ProductOption> options = request.options().stream()
                .map(optionRequest -> resolveOption(optionRequest, product, currentOptionById))
                .toList();
        deleteMissingOptions(request, currentOptions);
        return productOptionRepository.saveAll(options);
    }

    private ProductOption resolveOption(
            ProductOptionUpdateRequest request,
            Product product,
            Map<Long, ProductOption> currentOptionById
    ) {
        if (Objects.isNull(request.id())) {
            return request.toEntity(product);
        }
        ProductOption option = currentOptionById.get(request.id());
        if (Objects.isNull(option)) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
        request.applyTo(option);
        return option;
    }

    private void deleteMissingOptions(ProductUpdateRequest request, List<ProductOption> currentOptions) {
        List<Long> requestIds = request.options().stream()
                .map(ProductOptionUpdateRequest::id)
                .filter(Objects::nonNull)
                .toList();
        List<ProductOption> deletedOptions = currentOptions.stream()
                .filter(option -> !requestIds.contains(option.getId()))
                .toList();
        productOptionRepository.deleteAll(deletedOptions);
    }

    private List<ProductVariant> saveUpdatedVariants(
            ProductUpdateRequest request,
            Product product,
            List<ProductVariant> currentVariants
    ) {
        Map<Long, ProductVariant> currentVariantById = currentVariants.stream()
                .collect(Collectors.toMap(ProductVariant::getId, Function.identity()));
        boolean hasOptions = hasOptions(request.options());
        List<ProductVariant> variants = request.variants().stream()
                .map(variantRequest -> resolveVariant(variantRequest, product, currentVariantById, hasOptions))
                .toList();
        deleteMissingVariants(request, currentVariants);
        return productVariantRepository.saveAll(variants);
    }

    private ProductVariant resolveVariant(
            ProductVariantUpdateRequest request,
            Product product,
            Map<Long, ProductVariant> currentVariantById,
            boolean hasOptions
    ) {
        if (Objects.isNull(request.id())) {
            ProductVariant variant = request.toEntity(product);
            applyDefaultVariantTitle(variant, hasOptions);
            return variant;
        }
        ProductVariant variant = currentVariantById.get(request.id());
        if (Objects.isNull(variant)) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
        request.applyTo(variant);
        applyDefaultVariantTitle(variant, hasOptions);
        return variant;
    }

    private void applyDefaultVariantTitle(ProductVariant variant, boolean hasOptions) {
        if (!hasOptions) {
            variant.setTitle(PRODUCT_VARIANT_DEFAULT_TITLE);
        }
    }

    private boolean hasOptions(List<?> options) {
        return Objects.nonNull(options) && !options.isEmpty();
    }

    private void deleteMissingVariants(ProductUpdateRequest request, List<ProductVariant> currentVariants) {
        List<Long> requestIds = request.variants().stream()
                .map(ProductVariantUpdateRequest::id)
                .filter(Objects::nonNull)
                .toList();
        List<ProductVariant> deletedVariants = currentVariants.stream()
                .filter(variant -> !requestIds.contains(variant.getId()))
                .toList();
        productVariantRepository.deleteAll(deletedVariants);
    }

}
