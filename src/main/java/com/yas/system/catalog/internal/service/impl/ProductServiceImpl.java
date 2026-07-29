package com.yas.system.catalog.internal.service.impl;

import com.yas.system.catalog.internal.dto.request.ProductAttributeValueCreateRequest;
import com.yas.system.catalog.internal.dto.request.ProductAttributeValueUpdateRequest;
import com.yas.system.catalog.internal.dto.request.ProductCreateRequest;
import com.yas.system.catalog.internal.dto.request.ProductOptionCombinationCreateRequest;
import com.yas.system.catalog.internal.dto.request.ProductUpdateRequest;
import com.yas.system.catalog.internal.dto.request.ProductVariantCreateRequest;
import com.yas.system.catalog.internal.dto.request.ProductVariantUpdateRequest;
import com.yas.system.catalog.internal.dto.request.VariantOptionValueRequest;
import com.yas.system.catalog.internal.dto.response.ProductOptionCombinationResponse;
import com.yas.system.catalog.internal.dto.response.ProductResponse;
import com.yas.system.catalog.internal.dto.response.VariantOptionValueResponse;
import com.yas.system.catalog.internal.entity.Brand;
import com.yas.system.catalog.internal.entity.Category;
import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.entity.productCategory.ProductCategory;
import com.yas.system.catalog.internal.entity.productCategory.ProductCategoryId;
import com.yas.system.catalog.internal.entity.option.ProductOption;
import com.yas.system.catalog.internal.entity.option.ProductOptionCombination;
import com.yas.system.catalog.internal.entity.option.ProductOptionCombinationId;
import com.yas.system.catalog.internal.entity.variant.ProductVariant;
import com.yas.system.catalog.internal.entity.variant.VariantOptionValue;
import com.yas.system.catalog.internal.entity.attribute.ProductAttribute;
import com.yas.system.catalog.internal.entity.attribute.ProductAttributeValue;
import com.yas.system.catalog.internal.helper.ProductHelper;
import com.yas.system.catalog.internal.repository.BrandRepository;
import com.yas.system.catalog.internal.repository.CategoryRepository;
import com.yas.system.catalog.internal.repository.ProductAttributeRepository;
import com.yas.system.catalog.internal.repository.ProductAttributeValueRepository;
import com.yas.system.catalog.internal.repository.ProductCategoryRepository;
import com.yas.system.catalog.internal.repository.ProductOptionCombinationRepository;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    ProductOptionCombinationRepository productOptionCombinationRepository;
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
        log.info("Creating new product with name: {}, slug: {}", request.name(), request.slug());

        // Step 1: Validate product request (DB name/slug uniqueness check)
        validateProductRequest(request);

        // Step 2: Resolve optional category and brand entities
        Category category = resolveCategory(request.categoryId());
        Brand brand = resolveBrand(request.brandId());

        // Step 3: Create and save base Product entity
        Product savedProduct = productRepository.save(productHelper.createProduct(request, brand));
        log.info("Saved base product entity with ID: {}", savedProduct.getId());

        // Step 4: Collect category hierarchy and save product categories
        List<Category> categoryHierarchy = collectCategoryAndParents(category);
        saveProductCategories(savedProduct, categoryHierarchy);

        // Step 5: Save product option combinations
        List<ProductOptionCombination> savedOptionCombinations = saveProductOptionCombinations(request.options(), savedProduct);

        // Step 6: Save product attribute values
        List<ProductAttributeValue> savedAttributes = saveProductAttributeValues(request, savedProduct);

        // Step 7: Save product variants and associated variant option values
        List<ProductVariant> savedVariants = new ArrayList<>();
        List<VariantOptionValue> savedVariantOptionValues = new ArrayList<>();
        saveVariants(request, savedProduct, savedVariants, savedVariantOptionValues);
        log.info("Created {} variants for product ID: {}", savedVariants.size(), savedProduct.getId());

        // Step 8: Build product option combination responses
        List<ProductOptionCombinationResponse> options = buildOptionCombinationResponses(savedOptionCombinations, savedVariantOptionValues);

        // Step 9: Build and return ProductResponse
        return ProductResponse.from(savedProduct, savedAttributes, options, savedVariants, savedVariantOptionValues);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(ProductUpdateRequest request, Long productId) {
        log.info("Updating product with ID: {}", productId);

        // Step 1: Validate product update request (DB name/slug uniqueness check)
        validateProductRequest(request, productId);

        // Step 2: Resolve updated category and brand entities
        Category category = resolveCategory(request.categoryId());
        Brand brand = resolveBrand(request.brandId());

        // Step 3: Fetch existing product entity and apply updates
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        productHelper.updateProduct(request, product, brand);
        Product savedProduct = productRepository.save(product);
        log.info("Updated base product fields for ID: {}", productId);

        // Step 4: Update product categories (re-link category hierarchy)
        productCategoryRepository.deleteByProductId(productId);
        List<Category> categoryHierarchy = collectCategoryAndParents(category);
        saveProductCategories(savedProduct, categoryHierarchy);

        // Step 5: Update product option combinations
        productOptionCombinationRepository.deleteByProductId(productId);
        List<ProductOptionCombination> savedOptionCombinations = saveProductOptionCombinations(request.options(), savedProduct);

        // Step 6: Perform delta update on product attributes
        List<ProductAttributeValue> currentAttributes = productAttributeValueRepository.findByProductId(productId);
        List<ProductAttributeValue> savedAttributes = saveUpdatedProductAttributeValues(
                request,
                savedProduct,
                currentAttributes
        );

        // Step 7: Perform in-place delta update on product variants and variant option values
        List<ProductVariant> currentVariants = productVariantRepository.findByProductId(productId);
        List<VariantOptionValue> currentOptionValues = variantOptionValueRepository.findByProductVariantProductId(productId);

        List<ProductVariant> savedVariants = new ArrayList<>();
        List<VariantOptionValue> savedVariantOptionValues = new ArrayList<>();
        saveUpdatedVariants(
                request,
                savedProduct,
                currentVariants,
                currentOptionValues,
                savedVariants,
                savedVariantOptionValues
        );
        log.info("Updated {} variants and option values for product ID: {}", savedVariants.size(), productId);

        // Step 8: Build product option combination responses
        List<ProductOptionCombinationResponse> options = buildOptionCombinationResponses(savedOptionCombinations, savedVariantOptionValues);

        // Step 9: Build and return updated ProductResponse
        return ProductResponse.from(savedProduct, savedAttributes, options, savedVariants, savedVariantOptionValues);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        List<ProductAttributeValue> attributes = productAttributeValueRepository.findByProductId(id);
        List<ProductVariant> variants = productVariantRepository.findByProductId(id);
        List<VariantOptionValue> variantOptionValues = variantOptionValueRepository.findByProductVariantProductId(id);
        List<ProductOptionCombination> combinations = productOptionCombinationRepository.findByProductIdOrderByPositionAsc(id);

        List<ProductOptionCombinationResponse> options = buildOptionCombinationResponses(combinations, variantOptionValues);

        return ProductResponse.from(product, attributes, options, variants, variantOptionValues);
    }

    // Helper: Builds ProductOptionCombinationResponse DTO list for ProductResponse
    private List<ProductOptionCombinationResponse> buildOptionCombinationResponses(
            List<ProductOptionCombination> combinations,
            List<VariantOptionValue> variantOptionValues
    ) {
        if (Objects.nonNull(combinations) && !combinations.isEmpty()) {
            return combinations.stream()
                    .map(combination -> {
                        Long optionId = combination.getProductOption().getId();
                        List<String> values = variantOptionValues.stream()
                                .filter(vov -> Objects.nonNull(vov.getProductOption()) && Objects.equals(vov.getProductOption().getId(), optionId))
                                .map(VariantOptionValue::getValue)
                                .filter(val -> Objects.nonNull(val) && !val.isBlank())
                                .distinct()
                                .toList();
                        return new ProductOptionCombinationResponse(
                                optionId,
                                combination.getPosition(),
                                values
                        );
                    })
                    .toList();
        }

        List<ProductOption> distinctOptions = variantOptionValues.stream()
                .map(VariantOptionValue::getProductOption)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        return distinctOptions.stream()
                .map(option -> {
                    Long optionId = option.getId();
                    List<String> values = variantOptionValues.stream()
                            .filter(vov -> Objects.nonNull(vov.getProductOption()) && Objects.equals(vov.getProductOption().getId(), optionId))
                            .map(VariantOptionValue::getValue)
                            .filter(val -> Objects.nonNull(val) && !val.isBlank())
                            .distinct()
                            .toList();
                    return new ProductOptionCombinationResponse(
                            optionId,
                            0,
                            values
                    );
                })
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        productCategoryRepository.deleteByProductId(id);
        productAttributeValueRepository.deleteByProductId(id);
        productOptionCombinationRepository.deleteByProductId(id);
        variantOptionValueRepository.deleteByProductId(id);
        productVariantRepository.deleteByProductId(id);
        productRepository.delete(product);
    }

    // Helper: Creates and saves ProductOptionCombination entities linking Product and ProductOption at specific positions
    private List<ProductOptionCombination> saveProductOptionCombinations(
            List<ProductOptionCombinationCreateRequest> optionRequests,
            Product product
    ) {
        if (Objects.isNull(optionRequests) || optionRequests.isEmpty()) {
            return List.of();
        }
        List<Long> optionIds = optionRequests.stream()
                .map(ProductOptionCombinationCreateRequest::productOptionId)
                .toList();
        Map<Long, ProductOption> productOptionById = productOptionRepository.findAllById(optionIds).stream()
                .collect(Collectors.toMap(ProductOption::getId, Function.identity()));

        if (productOptionById.size() != optionIds.stream().distinct().count()) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_OPTION_NOT_FOUND);
        }

        List<ProductOptionCombination> combinations = optionRequests.stream()
                .map(optionRequest -> {
                    ProductOption option = productOptionById.get(optionRequest.productOptionId());
                    return ProductOptionCombination.builder()
                            .id(new ProductOptionCombinationId(product.getId(), option.getId()))
                            .product(product)
                            .productOption(option)
                            .position(optionRequest.position())
                            .build();
                })
                .toList();

        log.debug("Saving {} product option combinations for product ID: {}", combinations.size(), product.getId());
        return productOptionCombinationRepository.saveAll(combinations);
    }

    // Helper: Resolves Category entity by ID from DB
    private Category resolveCategory(Integer categoryId) {
        if (Objects.isNull(categoryId)) {
            return null;
        }
        log.debug("Resolving category with ID: {}", categoryId);
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    // Helper: Resolves Brand entity by ID from DB
    private Brand resolveBrand(Integer brandId) {
        if (Objects.isNull(brandId)) {
            return null;
        }
        log.debug("Resolving brand with ID: {}", brandId);
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BRAND_NOT_FOUND));
    }

    // Helper: Recursively collects a category and all of its parent categories up to the root
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
        log.debug("Collected {} category hierarchy entries for category ID: {}", categories.size(), category.getId());
        return categories;
    }

    // Helper: Persists ProductCategory junction records for product and category hierarchy
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
        log.debug("Saving {} product category junction records for product ID: {}", productCategories.size(), product.getId());
        return productCategoryRepository.saveAll(productCategories);
    }

    // Helper: Creates and saves ProductAttributeValue entities for product creation
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
        log.debug("Saving {} attribute values for product ID: {}", attributeValues.size(), product.getId());
        return productAttributeValueRepository.saveAll(attributeValues);
    }

    // Helper: Performs delta update on ProductAttributeValue entities for product update
    private List<ProductAttributeValue> saveUpdatedProductAttributeValues(
            ProductUpdateRequest request,
            Product product,
            List<ProductAttributeValue> currentAttributes
    ) {
        if (Objects.isNull(request.attributes()) || request.attributes().isEmpty()) {
            log.debug("Clearing all {} attribute values for product ID: {}", currentAttributes.size(), product.getId());
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
        log.debug("Saving {} updated attribute values for product ID: {}", attributeValues.size(), product.getId());
        return productAttributeValueRepository.saveAll(attributeValues);
    }

    // Helper: Fetches and validates ProductAttribute entities by IDs
    private Map<Long, ProductAttribute> findProductAttributes(List<Long> productAttributeIds) {
        log.debug("Fetching {} product attributes from DB", productAttributeIds.size());
        Map<Long, ProductAttribute> productAttributeById = productAttributeRepository.findAllById(productAttributeIds)
                .stream()
                .collect(Collectors.toMap(ProductAttribute::getId, Function.identity()));
        if (productAttributeById.size() != productAttributeIds.stream().distinct().count()) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_ATTRIBUTE_NOT_FOUND);
        }
        return productAttributeById;
    }

    // Helper: Resolves existing ProductAttributeValue entity to update or creates a new one
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

    // Helper: Deletes existing product attribute values that are omitted from the update request
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
        if (!deletedAttributes.isEmpty()) {
            log.debug("Deleting {} omitted product attribute values", deletedAttributes.size());
            productAttributeValueRepository.deleteAll(deletedAttributes);
        }
    }

    // Helper: Iterates over variant creation requests, saving ProductVariant entities and option values
    private void saveVariants(
            ProductCreateRequest request,
            Product product,
            List<ProductVariant> savedVariants,
            List<VariantOptionValue> savedOptionValues
    ) {
        log.debug("Saving {} variants for product ID: {}", request.variants().size(), product.getId());
        Set<Long> optionIds = request.variants().stream()
                .map(ProductVariantCreateRequest::options)
                .filter(Objects::nonNull)
                .flatMap(map -> map.keySet().stream())
                .collect(Collectors.toSet());
        Map<Long, ProductOption> productOptionById = optionIds.isEmpty()
                ? Map.of()
                : productOptionRepository.findAllById(optionIds).stream()
                        .collect(Collectors.toMap(ProductOption::getId, Function.identity()));

        for (ProductVariantCreateRequest variantRequest : request.variants()) {
            ProductVariant variant = variantRequest.toEntity(product);
            applyDefaultVariantTitle(variant);
            ProductVariant savedVariant = productVariantRepository.save(variant);
            savedVariants.add(savedVariant);

            if (Objects.nonNull(variantRequest.options())) {
                for (Map.Entry<Long, VariantOptionValueRequest> entry : variantRequest.options().entrySet()) {
                    Long optionId = entry.getKey();
                    VariantOptionValueRequest optValReq = entry.getValue();
                    if (Objects.isNull(optValReq) || isBlank(optValReq.value())) {
                        continue;
                    }
                    ProductOption option = productOptionById.get(optionId);
                    if (Objects.isNull(option)) {
                        throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
                    }
                    VariantOptionValue optionValue = VariantOptionValue.builder()
                            .value(optValReq.value())
                            .position(optValReq.position())
                            .productVariant(savedVariant)
                            .productOption(option)
                            .build();
                    savedOptionValues.add(variantOptionValueRepository.save(optionValue));
                }
            }
        }
        log.debug("Saved {} variant option values for product ID: {}", savedOptionValues.size(), product.getId());
    }

    // Helper: Performs in-place delta update on ProductVariant and VariantOptionValue entities
    private void saveUpdatedVariants(
            ProductUpdateRequest request,
            Product product,
            List<ProductVariant> currentVariants,
            List<VariantOptionValue> currentOptionValues,
            List<ProductVariant> savedVariants,
            List<VariantOptionValue> savedOptionValues
    ) {
        log.debug("Updating variants for product ID: {}", product.getId());
        Set<Long> optionIds = request.variants().stream()
                .map(ProductVariantUpdateRequest::options)
                .filter(Objects::nonNull)
                .flatMap(map -> map.keySet().stream())
                .collect(Collectors.toSet());
        Map<Long, ProductOption> productOptionById = optionIds.isEmpty()
                ? Map.of()
                : productOptionRepository.findAllById(optionIds).stream()
                        .collect(Collectors.toMap(ProductOption::getId, Function.identity()));

        Map<Long, ProductVariant> currentVariantById = currentVariants.stream()
                .collect(Collectors.toMap(ProductVariant::getId, Function.identity()));

        Map<String, VariantOptionValue> existingOptionValueMap = currentOptionValues.stream()
                .filter(vov -> Objects.nonNull(vov.getProductVariant()) && Objects.nonNull(vov.getProductOption()))
                .collect(Collectors.toMap(
                        vov -> vov.getProductVariant().getId() + "_" + vov.getProductOption().getId(),
                        Function.identity(),
                        (e1, e2) -> e1
                ));

        Set<Long> processedOptionValueIds = new HashSet<>();

        for (ProductVariantUpdateRequest variantRequest : request.variants()) {
            ProductVariant variant = resolveVariant(variantRequest, product, currentVariantById);
            ProductVariant savedVariant = productVariantRepository.save(variant);
            savedVariants.add(savedVariant);

            if (Objects.nonNull(variantRequest.options())) {
                for (Map.Entry<Long, VariantOptionValueRequest> entry : variantRequest.options().entrySet()) {
                    Long optionId = entry.getKey();
                    VariantOptionValueRequest optValReq = entry.getValue();
                    if (Objects.isNull(optValReq) || isBlank(optValReq.value())) {
                        continue;
                    }
                    String value = optValReq.value();
                    int position = optValReq.position();

                    String key = savedVariant.getId() != null ? savedVariant.getId() + "_" + optionId : null;
                    VariantOptionValue existingOptionValue = key != null ? existingOptionValueMap.get(key) : null;

                    if (Objects.nonNull(existingOptionValue)) {
                        processedOptionValueIds.add(existingOptionValue.getId());
                        if (!Objects.equals(existingOptionValue.getValue(), value) || existingOptionValue.getPosition() != position) {
                            log.debug("Updating VariantOptionValue ID: {} (option ID: {}, new value: '{}', position: {})", existingOptionValue.getId(), optionId, value, position);
                            existingOptionValue.setValue(value);
                            existingOptionValue.setPosition(position);
                        } else {
                            log.debug("Keeping unchanged VariantOptionValue ID: {} (option ID: {}, value: '{}')", existingOptionValue.getId(), optionId, value);
                        }
                        savedOptionValues.add(variantOptionValueRepository.save(existingOptionValue));
                    } else {
                        ProductOption option = productOptionById.get(optionId);
                        if (Objects.isNull(option)) {
                            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
                        }
                        VariantOptionValue optionValue = VariantOptionValue.builder()
                                .value(value)
                                .position(position)
                                .productVariant(savedVariant)
                                .productOption(option)
                                .build();
                        VariantOptionValue savedVov = variantOptionValueRepository.save(optionValue);
                        log.debug("Created new VariantOptionValue ID: {} (option ID: {}, value: '{}', position: {}) for variant ID: {}", savedVov.getId(), optionId, value, position, savedVariant.getId());
                        savedOptionValues.add(savedVov);
                    }
                }
            }
        }

        List<VariantOptionValue> deletedOptionValues = currentOptionValues.stream()
                .filter(vov -> !processedOptionValueIds.contains(vov.getId()))
                .toList();
        if (!deletedOptionValues.isEmpty()) {
            List<Long> deletedVovIds = deletedOptionValues.stream().map(VariantOptionValue::getId).toList();
            log.debug("Deleting {} omitted variant option values (IDs: {}) for product ID: {}", deletedOptionValues.size(), deletedVovIds, product.getId());
            variantOptionValueRepository.deleteAll(deletedOptionValues);
        }

        deleteMissingVariants(request, currentVariants);
    }

    // Helper: Validates product name and slug uniqueness for product creation
    private void validateProductRequest(ProductCreateRequest request) {
        log.debug("Validating uniqueness of name: '{}' and slug: '{}' for product creation", request.name(), request.slug());
        if (productRepository.existsByName(request.name()) || productRepository.existsBySlug(request.slug())) {
            throw new InvalidDataException(ErrorCode.PRODUCT_ALREADY_EXISTS);
        }
    }

    // Helper: Validates product name and slug uniqueness for product update
    private void validateProductRequest(ProductUpdateRequest request, Long productId) {
        log.debug("Validating uniqueness of name: '{}' and slug: '{}' for product update ID: {}", request.name(), request.slug(), productId);
        if (productRepository.existsByNameAndIdNot(request.name(), productId)
                || productRepository.existsBySlugAndIdNot(request.slug(), productId)) {
            throw new InvalidDataException(ErrorCode.PRODUCT_ALREADY_EXISTS);
        }
    }

    // Helper: Resolves existing ProductVariant entity to update or constructs a new one
    private ProductVariant resolveVariant(
            ProductVariantUpdateRequest request,
            Product product,
            Map<Long, ProductVariant> currentVariantById
    ) {
        if (Objects.isNull(request.id())) {
            ProductVariant variant = request.toEntity(product);
            applyDefaultVariantTitle(variant);
            log.debug("Creating new ProductVariant for product ID: {}", product.getId());
            return variant;
        }
        ProductVariant variant = currentVariantById.get(request.id());
        if (Objects.isNull(variant)) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
        request.applyTo(variant);
        applyDefaultVariantTitle(variant);
        log.debug("Updating existing ProductVariant ID: {} for product ID: {}", variant.getId(), product.getId());
        return variant;
    }

    // Helper: Applies default title to ProductVariant if title is blank
    private void applyDefaultVariantTitle(ProductVariant variant) {
        if (isBlank(variant.getTitle())) {
            variant.setTitle(PRODUCT_VARIANT_DEFAULT_TITLE);
        }
    }

    // Helper: Deletes existing variants omitted from update request
    private void deleteMissingVariants(ProductUpdateRequest request, List<ProductVariant> currentVariants) {
        List<Long> requestIds = request.variants().stream()
                .map(ProductVariantUpdateRequest::id)
                .filter(Objects::nonNull)
                .toList();
        List<ProductVariant> deletedVariants = currentVariants.stream()
                .filter(variant -> !requestIds.contains(variant.getId()))
                .toList();
        if (!deletedVariants.isEmpty()) {
            List<Long> deletedVariantIds = deletedVariants.stream().map(ProductVariant::getId).toList();
            log.debug("Deleting {} omitted product variants (IDs: {})", deletedVariants.size(), deletedVariantIds);
            productVariantRepository.deleteAll(deletedVariants);
        }
    }

}
