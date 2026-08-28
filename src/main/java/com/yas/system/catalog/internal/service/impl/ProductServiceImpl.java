package com.yas.system.catalog.internal.service.impl;

import com.yas.system.catalog.internal.dto.request.ProductAttributeValueCreateRequest;
import com.yas.system.catalog.internal.dto.request.ProductAttributeValueUpdateRequest;
import com.yas.system.catalog.internal.dto.request.ProductCreateRequest;
import com.yas.system.catalog.internal.dto.request.ProductOptionCombinationCreateRequest;
import com.yas.system.catalog.internal.dto.request.ProductOptionCombinationUpdateRequest;
import com.yas.system.catalog.internal.dto.request.ProductUpdateRequest;
import com.yas.system.catalog.internal.dto.request.ProductVariantAttributeValueCreateRequest;
import com.yas.system.catalog.internal.dto.request.ProductVariantAttributeValueUpdateRequest;
import com.yas.system.catalog.internal.dto.request.ProductVariantCreateRequest;
import com.yas.system.catalog.internal.dto.request.ProductVariantUpdateRequest;
import com.yas.system.catalog.internal.dto.request.ProductMediaRequest;
import com.yas.system.catalog.internal.dto.request.ProductOptionValueCreateRequest;
import com.yas.system.catalog.internal.dto.request.ProductOptionValueUpdateRequest;
import com.yas.system.catalog.internal.dto.response.ProductOptionCombinationResponse;
import com.yas.system.catalog.internal.dto.response.ProductResponse;
import com.yas.system.catalog.internal.dto.response.ProductOptionValueResponse;
import com.yas.system.catalog.internal.dto.response.ProductThumbnailResponse;
import com.yas.system.catalog.internal.specification.ProductSpecification;
import com.yas.system.common.response.PageResponse;
import com.yas.system.catalog.internal.entity.Brand;
import com.yas.system.catalog.internal.entity.Category;
import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.entity.ProductMedia;
import com.yas.system.media.api.MediaPublicService;
import com.yas.system.catalog.internal.entity.productCategory.ProductCategory;
import com.yas.system.catalog.internal.entity.productCategory.ProductCategoryId;
import com.yas.system.catalog.internal.entity.option.ProductOption;
import com.yas.system.catalog.internal.entity.option.ProductOptionCombination;
import com.yas.system.catalog.internal.entity.option.ProductOptionCombinationId;
import com.yas.system.catalog.internal.entity.option.ProductOptionValue;
import com.yas.system.catalog.internal.entity.variant.ProductVariant;
import com.yas.system.catalog.internal.entity.variant.VariantOptionValue;
import com.yas.system.catalog.internal.entity.attribute.ProductAttribute;
import com.yas.system.catalog.internal.entity.attribute.ProductAttributeValue;
import com.yas.system.catalog.internal.entity.attribute.ProductVariantAttributeValue;
import com.yas.system.catalog.internal.helper.ProductHelper;
import com.yas.system.catalog.internal.repository.BrandRepository;
import com.yas.system.catalog.internal.repository.CategoryRepository;
import com.yas.system.catalog.internal.repository.ProductAttributeRepository;
import com.yas.system.catalog.internal.repository.ProductAttributeValueRepository;
import com.yas.system.catalog.internal.repository.ProductCategoryRepository;
import com.yas.system.catalog.internal.repository.ProductMediaRepository;
import com.yas.system.catalog.internal.repository.ProductOptionCombinationRepository;
import com.yas.system.catalog.internal.repository.ProductOptionRepository;
import com.yas.system.catalog.internal.repository.ProductOptionValueRepository;
import com.yas.system.catalog.internal.repository.ProductRepository;
import com.yas.system.catalog.internal.repository.ProductVariantAttributeValueRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
    ProductVariantAttributeValueRepository productVariantAttributeValueRepository;
    ProductOptionRepository productOptionRepository;
    ProductOptionCombinationRepository productOptionCombinationRepository;
    ProductOptionValueRepository productOptionValueRepository;
    VariantOptionValueRepository variantOptionValueRepository;
    ProductAttributeRepository productAttributeRepository;
    ProductAttributeValueRepository productAttributeValueRepository;
    CategoryRepository categoryRepository;
    BrandRepository brandRepository;
    ProductCategoryRepository productCategoryRepository;
    ProductMediaRepository productMediaRepository;
    MediaPublicService mediaPublicService;
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

        // Step 5: Save product medias
        List<ProductMedia> savedMedias = saveProductMedias(request.medias(), savedProduct);

        // Step 6: Save product option combinations and values
        List<ProductOptionValue> savedOptionValues = new ArrayList<>();
        List<ProductOptionCombination> savedOptionCombinations = saveProductOptionCombinations(
                request.options(),
                savedProduct,
                savedOptionValues
        );

        // Step 7: Save product attribute values
        List<ProductAttributeValue> savedAttributes = saveProductAttributeValues(request, savedProduct);

        // Step 8: Save product variants, associated variant option values, and variant attribute values
        List<ProductVariant> savedVariants = new ArrayList<>();
        List<VariantOptionValue> savedVariantOptionValues = new ArrayList<>();
        List<ProductVariantAttributeValue> savedVariantAttributeValues = new ArrayList<>();
        saveVariants(request, savedProduct, savedOptionValues, savedVariants, savedVariantOptionValues, savedVariantAttributeValues);
        log.info("Created {} variants for product ID: {}", savedVariants.size(), savedProduct.getId());

        // Step 9: Build product option combination responses
        List<ProductOptionCombinationResponse> options = buildOptionCombinationResponses(savedOptionCombinations, savedOptionValues);

        // Step 10: Fetch media URLs via public Media service
        List<String> mediaIds = savedMedias.stream().map(ProductMedia::getMediaId).toList();
        Map<String, String> mediaUrlMap = mediaPublicService.getMediaUrls(mediaIds);

        // Step 11: Build and return ProductResponse
        return ProductResponse.from(savedProduct, savedMedias, mediaUrlMap, savedAttributes, options, savedVariants, savedVariantOptionValues, savedVariantAttributeValues);
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

        // Step 4: Perform delta update on product categories
        List<Category> categoryHierarchy = collectCategoryAndParents(category);
        List<ProductCategory> currentProductCategories = productCategoryRepository.findByProductId(productId);
        saveUpdatedProductCategories(savedProduct, categoryHierarchy, currentProductCategories);

        // Step 5: Perform delta update on product media
        List<ProductMedia> currentMedias = productMediaRepository.findByProductIdOrderByPositionAsc(productId);
        List<ProductMedia> savedMedias = saveUpdatedProductMedias(request.medias(), savedProduct, currentMedias);

        // Step 6: Perform delta update on product option combinations and values
        List<ProductOptionCombination> currentCombinations = productOptionCombinationRepository.findByProductIdOrderByPositionAsc(productId);
        List<ProductOptionValue> currentOptionValues = productOptionValueRepository.findByProductId(productId);

        List<ProductOptionValue> savedOptionValues = new ArrayList<>();
        List<ProductOptionCombination> savedOptionCombinations = saveUpdatedProductOptionCombinations(
                request.options(),
                savedProduct,
                currentCombinations,
                currentOptionValues,
                savedOptionValues
        );

        // Step 7: Perform delta update on product attributes
        List<ProductAttributeValue> currentAttributes = productAttributeValueRepository.findByProductId(productId);
        List<ProductAttributeValue> savedAttributes = saveUpdatedProductAttributeValues(
                request,
                savedProduct,
                currentAttributes
        );

        // Step 8: Perform in-place delta update on product variants, variant option values, and variant attribute values
        List<ProductVariant> currentVariants = productVariantRepository.findByProductId(productId);
        List<VariantOptionValue> currentOptionValuesList = variantOptionValueRepository.findByProductVariantProductId(productId);
        List<ProductVariantAttributeValue> currentVariantAttributes = productVariantAttributeValueRepository.findByProductVariantProductId(productId);

        List<ProductVariant> savedVariants = new ArrayList<>();
        List<VariantOptionValue> savedVariantOptionValues = new ArrayList<>();
        List<ProductVariantAttributeValue> savedVariantAttributeValues = new ArrayList<>();
        saveUpdatedVariants(
                request,
                savedProduct,
                savedOptionValues,
                currentVariants,
                currentOptionValuesList,
                currentVariantAttributes,
                savedVariants,
                savedVariantOptionValues,
                savedVariantAttributeValues
        );
        log.info("Updated {} variants, option values, and attribute values for product ID: {}", savedVariants.size(), productId);

        // Step 9: Clean up omitted option values and combinations AFTER variant option values have been cleaned up
        deleteOmittedOptionCombinationsAndValues(request.options(), currentCombinations, currentOptionValues, savedOptionValues);

        // Step 10: Build product option combination responses
        List<ProductOptionCombinationResponse> options = buildOptionCombinationResponses(savedOptionCombinations, savedOptionValues);

        // Step 11: Fetch media URLs via public Media service
        List<String> mediaIds = savedMedias.stream().map(ProductMedia::getMediaId).toList();
        Map<String, String> mediaUrlMap = mediaPublicService.getMediaUrls(mediaIds);

        // Step 12: Build and return updated ProductResponse
        return ProductResponse.from(savedProduct, savedMedias, mediaUrlMap, savedAttributes, options, savedVariants, savedVariantOptionValues, savedVariantAttributeValues);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        List<ProductMedia> medias = productMediaRepository.findByProductIdOrderByPositionAsc(id);
        List<ProductAttributeValue> attributes = productAttributeValueRepository.findByProductId(id);
        List<ProductVariant> variants = productVariantRepository.findByProductId(id);
        List<VariantOptionValue> variantOptionValues = variantOptionValueRepository.findByProductVariantProductId(id);
        List<ProductVariantAttributeValue> variantAttributeValues = productVariantAttributeValueRepository.findByProductVariantProductId(id);
        List<ProductOptionCombination> combinations = productOptionCombinationRepository.findByProductIdOrderByPositionAsc(id);
        List<ProductOptionValue> optionValues = productOptionValueRepository.findByProductId(id);

        List<ProductOptionCombinationResponse> options = buildOptionCombinationResponses(combinations, optionValues);

        List<String> mediaIds = medias.stream().map(ProductMedia::getMediaId).toList();
        Map<String, String> mediaUrlMap = mediaPublicService.getMediaUrls(mediaIds);

        return ProductResponse.from(product, medias, mediaUrlMap, attributes, options, variants, variantOptionValues, variantAttributeValues);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductThumbnailResponse> getProducts(
            int pageNo,
            int pageSize,
            String name,
            Integer categoryId,
            Integer brandId
    ) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
        Specification<Product> spec = Specification.where(ProductSpecification.hasName(name))
                .and(ProductSpecification.hasCategoryId(categoryId))
                .and(ProductSpecification.hasBrandId(brandId));

        Page<Product> productPage = productRepository.findAll(spec, pageable);

        List<ProductThumbnailResponse> content = productPage.getContent().stream()
                .map(ProductThumbnailResponse::from)
                .toList();

        return new PageResponse<>(
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalPages(),
                productPage.getTotalElements(),
                content
        );
    }

    // Helper: Builds ProductOptionCombinationResponse DTO list for ProductResponse
    private List<ProductOptionCombinationResponse> buildOptionCombinationResponses(
            List<ProductOptionCombination> combinations,
            List<ProductOptionValue> optionValues
    ) {
        if (Objects.isNull(combinations) || combinations.isEmpty()) {
            return List.of();
        }

        Map<Long, List<ProductOptionValueResponse>> valuesByOptionId = Objects.isNull(optionValues)
                ? Map.of()
                : optionValues.stream()
                        .filter(pov -> Objects.nonNull(pov.getProductOptionCombination())
                                && Objects.nonNull(pov.getProductOptionCombination().getProductOption()))
                        .collect(Collectors.groupingBy(
                                pov -> pov.getProductOptionCombination().getProductOption().getId(),
                                Collectors.mapping(
                                        pov -> new ProductOptionValueResponse(
                                                pov.getId(),
                                                pov.getValue(),
                                                pov.getPosition()
                                        ),
                                        Collectors.toList()
                                )
                        ));

        return combinations.stream()
                .filter(combination -> Objects.nonNull(combination) && Objects.nonNull(combination.getProductOption()))
                .map(combination -> {
                    Long optionId = combination.getProductOption().getId();
                    String name = combination.getProductOption().getName();
                    List<ProductOptionValueResponse> values = valuesByOptionId.getOrDefault(optionId, List.of());
                    return new ProductOptionCombinationResponse(
                            optionId,
                            name,
                            combination.getPosition(),
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

        productMediaRepository.deleteByProductId(id);
        productCategoryRepository.deleteByProductId(id);
        productAttributeValueRepository.deleteByProductId(id);
        productOptionValueRepository.deleteByProductId(id);
        productOptionCombinationRepository.deleteByProductId(id);
        productVariantAttributeValueRepository.deleteByProductVariantProductId(id);
        variantOptionValueRepository.deleteByProductId(id);
        productVariantRepository.deleteByProductId(id);
        productRepository.delete(product);
    }

    // Helper: Creates and saves ProductMedia entities for product creation
    private List<ProductMedia> saveProductMedias(
            List<ProductMediaRequest> mediaRequests,
            Product product
    ) {
        if (Objects.isNull(mediaRequests) || mediaRequests.isEmpty()) {
            return List.of();
        }
        List<ProductMedia> mediasToSave = mediaRequests.stream()
                .filter(req -> Objects.nonNull(req) && !isBlank(req.mediaId()))
                .map(req -> ProductMedia.builder()
                        .mediaId(req.mediaId())
                        .position(req.position())
                        .product(product)
                        .build())
                .toList();
        log.debug("Saving {} product medias for product ID: {}", mediasToSave.size(), product.getId());
        return productMediaRepository.saveAll(mediasToSave);
    }

    // Helper: Performs delta update on ProductMedia entities for product update
    private List<ProductMedia> saveUpdatedProductMedias(
            List<ProductMediaRequest> mediaRequests,
            Product product,
            List<ProductMedia> currentMedias
    ) {
        if (Objects.isNull(mediaRequests) || mediaRequests.isEmpty()) {
            if (Objects.nonNull(currentMedias) && !currentMedias.isEmpty()) {
                log.debug("Deleting all {} existing product medias for product ID: {}", currentMedias.size(), product.getId());
                productMediaRepository.deleteAll(currentMedias);
            }
            return List.of();
        }

        Map<String, ProductMedia> currentMediaMap = Objects.isNull(currentMedias)
                ? Map.of()
                : currentMedias.stream().collect(Collectors.toMap(ProductMedia::getMediaId, Function.identity(), (e1, _) -> e1));

        List<ProductMedia> toSave = new ArrayList<>();
        Set<String> requestedMediaIds = new HashSet<>();

        for (ProductMediaRequest req : mediaRequests) {
            if (Objects.isNull(req) || isBlank(req.mediaId())) {
                continue;
            }
            requestedMediaIds.add(req.mediaId());
            ProductMedia existingMedia = currentMediaMap.get(req.mediaId());
            if (Objects.nonNull(existingMedia)) {
                existingMedia.setPosition(req.position());
                toSave.add(existingMedia);
            } else {
                toSave.add(ProductMedia.builder()
                        .mediaId(req.mediaId())
                        .position(req.position())
                        .product(product)
                        .build());
            }
        }

        if (Objects.nonNull(currentMedias)) {
            List<ProductMedia> toDelete = currentMedias.stream()
                    .filter(media -> !requestedMediaIds.contains(media.getMediaId()))
                    .toList();
            if (!toDelete.isEmpty()) {
                log.debug("Deleting {} omitted product medias for product ID: {}", toDelete.size(), product.getId());
                productMediaRepository.deleteAll(toDelete);
            }
        }

        return productMediaRepository.saveAll(toSave);
    }

    // Helper: Creates and saves ProductOptionCombination and ProductOptionValue entities for product creation
    private List<ProductOptionCombination> saveProductOptionCombinations(
            List<ProductOptionCombinationCreateRequest> optionRequests,
            Product product,
            List<ProductOptionValue> outOptionValues
    ) {
        if (Objects.isNull(optionRequests) || optionRequests.isEmpty()) {
            return List.of();
        }
        List<Long> optionIds = optionRequests.stream()
                .map(ProductOptionCombinationCreateRequest::productOptionId)
                .toList();
        Map<Long, ProductOption> productOptionById = findProductOptions(optionIds);

        List<ProductOptionCombination> combinations = optionRequests.stream()
                .map(optionRequest -> createProductOptionCombination(
                        product,
                        productOptionById.get(optionRequest.productOptionId()),
                        optionRequest.position()
                ))
                .toList();

        log.debug("Saving {} product option combinations for product ID: {}", combinations.size(), product.getId());
        List<ProductOptionCombination> savedCombinations = productOptionCombinationRepository.saveAll(combinations);

        List<ProductOptionValue> valuesToSave = buildProductOptionValuesToSave(optionRequests, savedCombinations);

        if (!valuesToSave.isEmpty()) {
            log.debug("Saving {} product option values for product ID: {}", valuesToSave.size(), product.getId());
            outOptionValues.addAll(productOptionValueRepository.saveAll(valuesToSave));
        }

        return savedCombinations;
    }

    // Helper: Builds list of ProductOptionValue entities for newly created combinations
    private List<ProductOptionValue> buildProductOptionValuesToSave(
            List<ProductOptionCombinationCreateRequest> optionRequests,
            List<ProductOptionCombination> savedCombinations
    ) {
        Map<Long, ProductOptionCombination> savedCombinationByOptionId = savedCombinations.stream()
                .collect(Collectors.toMap(poc -> poc.getProductOption().getId(), Function.identity(), (e1, _) -> e1));

        List<ProductOptionValue> valuesToSave = new ArrayList<>();
        for (ProductOptionCombinationCreateRequest optionRequest : optionRequests) {
            ProductOptionCombination combination = savedCombinationByOptionId.get(optionRequest.productOptionId());
            if (Objects.nonNull(optionRequest.values()) && Objects.nonNull(combination)) {
                for (ProductOptionValueCreateRequest valReq : optionRequest.values()) {
                    if (Objects.nonNull(valReq) && !isBlank(valReq.value())) {
                        valuesToSave.add(createProductOptionValue(valReq.value(), valReq.position(), combination));
                    }
                }
            }
        }
        return valuesToSave;
    }

    // Helper: Performs delta update on ProductOptionCombination and ProductOptionValue entities for product update
    private List<ProductOptionCombination> saveUpdatedProductOptionCombinations(
            List<ProductOptionCombinationUpdateRequest> optionRequests,
            Product product,
            List<ProductOptionCombination> currentCombinations,
            List<ProductOptionValue> currentOptionValues,
            List<ProductOptionValue> outOptionValues
    ) {
        if (Objects.isNull(optionRequests) || optionRequests.isEmpty()) {
            clearOptionCombinationsAndValues(product, currentCombinations, currentOptionValues);
            return List.of();
        }

        List<Long> optionIds = optionRequests.stream()
                .map(ProductOptionCombinationUpdateRequest::productOptionId)
                .toList();
        Map<Long, ProductOption> productOptionById = findProductOptions(optionIds);

        List<ProductOptionCombination> savedCombinations = upsertOptionCombinations(
                optionRequests, product, currentCombinations, productOptionById
        );

        saveUpdatedOptionValues(
                optionRequests, product, currentOptionValues, savedCombinations, optionIds, outOptionValues
        );

        return savedCombinations;
    }

    // Helper: Clears all existing option values and combinations when update request is empty
    private void clearOptionCombinationsAndValues(
            Product product,
            List<ProductOptionCombination> currentCombinations,
            List<ProductOptionValue> currentOptionValues
    ) {
        if (Objects.nonNull(currentOptionValues) && !currentOptionValues.isEmpty()) {
            log.debug("Clearing all {} product option values for product ID: {}", currentOptionValues.size(), product.getId());
            productOptionValueRepository.deleteAll(currentOptionValues);
        }
        if (Objects.nonNull(currentCombinations) && !currentCombinations.isEmpty()) {
            log.debug("Clearing all {} product option combinations for product ID: {}", currentCombinations.size(), product.getId());
            productOptionCombinationRepository.deleteAll(currentCombinations);
        }
    }

    // Helper: Deletes omitted product option values and combinations AFTER variant option values are cleaned up
    private void deleteOmittedOptionCombinationsAndValues(
            List<ProductOptionCombinationUpdateRequest> optionRequests,
            List<ProductOptionCombination> currentCombinations,
            List<ProductOptionValue> currentOptionValues,
            List<ProductOptionValue> savedOptionValues
    ) {
        if (Objects.isNull(currentOptionValues) && Objects.isNull(currentCombinations)) {
            return;
        }

        Set<Long> savedPovIds = Objects.isNull(savedOptionValues)
                ? Set.of()
                : savedOptionValues.stream()
                        .map(ProductOptionValue::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

        List<ProductOptionValue> deletedOptionValues = Objects.isNull(currentOptionValues)
                ? List.of()
                : currentOptionValues.stream()
                        .filter(pov -> Objects.nonNull(pov.getId()) && !savedPovIds.contains(pov.getId()))
                        .toList();

        if (!deletedOptionValues.isEmpty()) {
            log.debug("Deleting {} omitted product option values", deletedOptionValues.size());
            productOptionValueRepository.deleteAll(deletedOptionValues);
        }

        List<Long> requestedOptionIds = Objects.isNull(optionRequests)
                ? List.of()
                : optionRequests.stream()
                        .map(ProductOptionCombinationUpdateRequest::productOptionId)
                        .filter(Objects::nonNull)
                        .toList();

        Set<Long> targetOptionIds = new HashSet<>(requestedOptionIds);
        List<ProductOptionCombination> deletedCombinations = Objects.isNull(currentCombinations)
                ? List.of()
                : currentCombinations.stream()
                        .filter(poc -> Objects.nonNull(poc.getProductOption())
                                && !targetOptionIds.contains(poc.getProductOption().getId()))
                        .toList();

        if (!deletedCombinations.isEmpty()) {
            log.debug("Deleting {} omitted product option combinations", deletedCombinations.size());
            productOptionCombinationRepository.deleteAll(deletedCombinations);
        }
    }

    // Helper: Updates positions of existing combinations or builds new combinations, then saves them
    private List<ProductOptionCombination> upsertOptionCombinations(
            List<ProductOptionCombinationUpdateRequest> optionRequests,
            Product product,
            List<ProductOptionCombination> currentCombinations,
            Map<Long, ProductOption> productOptionById
    ) {
        Map<Long, ProductOptionCombination> currentByOptionId = Objects.isNull(currentCombinations)
                ? Map.of()
                : currentCombinations.stream()
                        .collect(Collectors.toMap(
                                poc -> poc.getProductOption().getId(),
                                Function.identity(),
                                (e1, _) -> e1
                        ));

        List<ProductOptionCombination> updatedCombinations = optionRequests.stream()
                .map(optionRequest -> {
                    Long optionId = optionRequest.productOptionId();
                    ProductOptionCombination existing = currentByOptionId.get(optionId);
                    if (Objects.nonNull(existing)) {
                        existing.setPosition(optionRequest.position());
                        return existing;
                    }
                    ProductOption option = productOptionById.get(optionId);
                    return createProductOptionCombination(product, option, optionRequest.position());
                })
                .toList();

        log.debug("Saving {} updated product option combinations for product ID: {}", updatedCombinations.size(), product.getId());
        return productOptionCombinationRepository.saveAll(updatedCombinations);
    }

    // Helper: Updates, creates, and deletes product option values according to update requests
    private void saveUpdatedOptionValues(
            List<ProductOptionCombinationUpdateRequest> optionRequests,
            Product product,
            List<ProductOptionValue> currentOptionValues,
            List<ProductOptionCombination> savedCombinations,
            List<Long> requestedOptionIds,
            List<ProductOptionValue> outOptionValues
    ) {
        Map<Long, ProductOptionCombination> savedCombinationByOptionId = savedCombinations.stream()
                .collect(Collectors.toMap(poc -> poc.getProductOption().getId(), Function.identity(), (e1, _) -> e1));

        Map<Long, ProductOptionValue> currentPovById = Objects.isNull(currentOptionValues)
                ? Map.of()
                : currentOptionValues.stream()
                        .filter(pov -> Objects.nonNull(pov.getId()))
                        .collect(Collectors.toMap(ProductOptionValue::getId, Function.identity(), (e1, _) -> e1));

        Set<Long> processedPovIds = new HashSet<>();
        List<ProductOptionValue> valuesToSave = new ArrayList<>();

        for (ProductOptionCombinationUpdateRequest optionRequest : optionRequests) {
            ProductOptionCombination combination = savedCombinationByOptionId.get(optionRequest.productOptionId());
            if (Objects.nonNull(optionRequest.values()) && Objects.nonNull(combination)) {
                for (ProductOptionValueUpdateRequest valReq : optionRequest.values()) {
                    if (Objects.isNull(valReq) || isBlank(valReq.value())) {
                        continue;
                    }
                    if (Objects.nonNull(valReq.id()) && currentPovById.containsKey(valReq.id())) {
                        ProductOptionValue existingPov = currentPovById.get(valReq.id());
                        existingPov.setValue(valReq.value());
                        existingPov.setPosition(valReq.position());
                        existingPov.setProductOptionCombination(combination);
                        processedPovIds.add(existingPov.getId());
                        valuesToSave.add(existingPov);
                    } else {
                        ProductOptionValue newPov = createProductOptionValue(valReq.value(), valReq.position(), combination);
                        valuesToSave.add(newPov);
                    }
                }
            }
        }

        if (!valuesToSave.isEmpty()) {
            log.debug("Saving {} updated product option values for product ID: {}", valuesToSave.size(), product.getId());
            outOptionValues.addAll(productOptionValueRepository.saveAll(valuesToSave));
        }
    }

    // Helper: Deletes product option values that were omitted from active combinations in update request
    private void deleteOmittedOptionValues(
            List<ProductOptionValue> currentOptionValues,
            Set<Long> targetOptionIds,
            Set<Long> processedPovIds
    ) {
        List<ProductOptionValue> deletedOptionValues = Objects.isNull(currentOptionValues)
                ? List.of()
                : currentOptionValues.stream()
                        .filter(pov -> Objects.nonNull(pov.getProductOptionCombination())
                                && targetOptionIds.contains(pov.getProductOptionCombination().getProductOption().getId())
                                && !processedPovIds.contains(pov.getId()))
                        .toList();

        if (!deletedOptionValues.isEmpty()) {
            log.debug("Deleting {} omitted product option values", deletedOptionValues.size());
            productOptionValueRepository.deleteAll(deletedOptionValues);
        }
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
                .map(category -> createProductCategory(product, category))
                .toList();
        log.debug("Saving {} product category junction records for product ID: {}", productCategories.size(), product.getId());
        return productCategoryRepository.saveAll(productCategories);
    }

    // Helper: Performs delta update on ProductCategory entities for product update
    private List<ProductCategory> saveUpdatedProductCategories(
            Product product,
            List<Category> targetCategories,
            List<ProductCategory> currentProductCategories
    ) {
        if (Objects.isNull(targetCategories) || targetCategories.isEmpty()) {
            if (Objects.nonNull(currentProductCategories) && !currentProductCategories.isEmpty()) {
                log.debug("Deleting all {} product category records for product ID: {}", currentProductCategories.size(), product.getId());
                productCategoryRepository.deleteAll(currentProductCategories);
            }
            return List.of();
        }

        deleteOmittedProductCategories(targetCategories, currentProductCategories);
        return upsertProductCategories(product, targetCategories, currentProductCategories);
    }

    // Helper: Deletes product category junction records that were omitted in the update request
    private void deleteOmittedProductCategories(
            List<Category> targetCategories,
            List<ProductCategory> currentProductCategories
    ) {
        if (Objects.isNull(currentProductCategories) || currentProductCategories.isEmpty()) {
            return;
        }
        Set<Integer> targetCategoryIds = targetCategories.stream()
                .map(Category::getId)
                .collect(Collectors.toSet());

        List<ProductCategory> deletedProductCategories = currentProductCategories.stream()
                .filter(pc -> !targetCategoryIds.contains(pc.getCategory().getId()))
                .toList();

        if (!deletedProductCategories.isEmpty()) {
            log.debug("Deleting {} omitted product category junction records", deletedProductCategories.size());
            productCategoryRepository.deleteAll(deletedProductCategories);
        }
    }

    // Helper: Upserts target product category junction records and persists them
    private List<ProductCategory> upsertProductCategories(
            Product product,
            List<Category> targetCategories,
            List<ProductCategory> currentProductCategories
    ) {
        Map<Integer, ProductCategory> currentByCategoryId = Objects.isNull(currentProductCategories)
                ? Map.of()
                : currentProductCategories.stream()
                        .collect(Collectors.toMap(
                                pc -> pc.getCategory().getId(),
                                Function.identity(),
                                (e1, _) -> e1
                        ));

        List<ProductCategory> updatedProductCategories = targetCategories.stream()
                .map(category -> {
                    ProductCategory existing = currentByCategoryId.get(category.getId());
                    return Objects.nonNull(existing) ? existing : createProductCategory(product, category);
                })
                .toList();

        log.debug("Saving {} updated product category records for product ID: {}", updatedProductCategories.size(), product.getId());
        return productCategoryRepository.saveAll(updatedProductCategories);
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

    // Helper: Iterates over variant creation requests, saving ProductVariant entities, option values, and attribute values
    private void saveVariants(
            ProductCreateRequest request,
            Product product,
            List<ProductOptionValue> productOptionValues,
            List<ProductVariant> savedVariants,
            List<VariantOptionValue> savedOptionValues,
            List<ProductVariantAttributeValue> savedVariantAttributeValues
    ) {
        if (Objects.isNull(request.variants()) || request.variants().isEmpty()) {
            return;
        }
        log.debug("Saving {} variants for product ID: {}", request.variants().size(), product.getId());

        List<ProductOptionCombinationCreateRequest> optionRequests = Objects.isNull(request.options())
                ? List.of()
                : request.options();
        Map<String, ProductOptionValue> povMap = mapOptionValuesByOptionAndValue(productOptionValues);

        List<Long> allVariantAttributeIds = request.variants().stream()
                .filter(v -> Objects.nonNull(v.attributeValues()))
                .flatMap(v -> v.attributeValues().stream())
                .map(ProductVariantAttributeValueCreateRequest::productAttributeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, ProductAttribute> productAttributeById = allVariantAttributeIds.isEmpty()
                ? Map.of()
                : findProductAttributes(allVariantAttributeIds);

        int variantIndex = 0;
        for (ProductVariantCreateRequest variantRequest : request.variants()) {
            ProductVariant variant = variantRequest.toEntity(product);
            applyDefaultVariantTitle(variant);
            ProductVariant savedVariant = productVariantRepository.save(variant);
            savedVariants.add(savedVariant);

            saveVariantOptionValuesForCreatedVariant(
                    savedVariant, variantIndex, optionRequests, povMap, savedOptionValues
            );
            saveVariantAttributeValuesForCreatedVariant(
                    savedVariant, variantRequest.attributeValues(), productAttributeById, savedVariantAttributeValues
            );
            variantIndex++;
        }
        log.debug("Saved {} variant option values and {} variant attribute values for product ID: {}",
                savedOptionValues.size(), savedVariantAttributeValues.size(), product.getId());
    }

    // Helper: Creates and saves ProductVariantAttributeValue entries for a newly created variant
    private void saveVariantAttributeValuesForCreatedVariant(
            ProductVariant savedVariant,
            List<ProductVariantAttributeValueCreateRequest> attributeRequests,
            Map<Long, ProductAttribute> productAttributeById,
            List<ProductVariantAttributeValue> savedVariantAttributeValues
    ) {
        if (Objects.isNull(attributeRequests) || attributeRequests.isEmpty()) {
            return;
        }
        List<ProductVariantAttributeValue> toSave = attributeRequests.stream()
                .filter(req -> Objects.nonNull(req) && Objects.nonNull(req.productAttributeId()))
                .map(req -> ProductVariantAttributeValue.builder()
                        .productVariant(savedVariant)
                        .productAttribute(productAttributeById.get(req.productAttributeId()))
                        .value(req.value())
                        .build())
                .toList();
        if (!toSave.isEmpty()) {
            savedVariantAttributeValues.addAll(productVariantAttributeValueRepository.saveAll(toSave));
        }
    }

    // Helper: Creates and saves VariantOptionValue entries for a newly created variant
    private void saveVariantOptionValuesForCreatedVariant(
            ProductVariant savedVariant,
            int variantIndex,
            List<ProductOptionCombinationCreateRequest> optionRequests,
            Map<String, ProductOptionValue> povMap,
            List<VariantOptionValue> savedOptionValues
    ) {
        for (ProductOptionCombinationCreateRequest optionReq : optionRequests) {
            if (Objects.isNull(optionReq.values()) || variantIndex >= optionReq.values().size()) {
                continue;
            }
            ProductOptionValueCreateRequest optValReq = optionReq.values().get(variantIndex);
            if (Objects.isNull(optValReq) || isBlank(optValReq.value())) {
                continue;
            }
            String key = optionReq.productOptionId() + "_" + optValReq.value();
            ProductOptionValue pov = povMap.get(key);
            if (Objects.nonNull(pov)) {
                VariantOptionValue optionValue = createVariantOptionValue(savedVariant, pov);
                savedOptionValues.add(variantOptionValueRepository.save(optionValue));
            }
        }
    }

    // Helper: Performs in-place delta update on ProductVariant, VariantOptionValue, and ProductVariantAttributeValue entities
    private void saveUpdatedVariants(
            ProductUpdateRequest request,
            Product product,
            List<ProductOptionValue> productOptionValues,
            List<ProductVariant> currentVariants,
            List<VariantOptionValue> currentOptionValues,
            List<ProductVariantAttributeValue> currentVariantAttributes,
            List<ProductVariant> savedVariants,
            List<VariantOptionValue> savedOptionValues,
            List<ProductVariantAttributeValue> savedVariantAttributeValues
    ) {
        if (Objects.isNull(request.variants()) || request.variants().isEmpty()) {
            return;
        }
        log.debug("Updating variants for product ID: {}", product.getId());

        Set<Long> processedOptionValueIds = new HashSet<>();
        Set<Long> processedAttributeValueIds = new HashSet<>();

        processUpdatedVariantRelations(
                request, product, productOptionValues, currentVariants, currentOptionValues, currentVariantAttributes,
                savedVariants, savedOptionValues, savedVariantAttributeValues,
                processedOptionValueIds, processedAttributeValueIds
        );

        deleteOmittedVariantOptionValues(product, currentOptionValues, processedOptionValueIds);
        deleteOmittedVariantAttributeValues(product, currentVariantAttributes, processedAttributeValueIds);
        deleteMissingVariants(request, currentVariants);
    }

    // Helper: Updates or creates variants and their associated variant option values and attribute values
    private void processUpdatedVariantRelations(
            ProductUpdateRequest request,
            Product product,
            List<ProductOptionValue> productOptionValues,
            List<ProductVariant> currentVariants,
            List<VariantOptionValue> currentOptionValues,
            List<ProductVariantAttributeValue> currentVariantAttributes,
            List<ProductVariant> savedVariants,
            List<VariantOptionValue> savedOptionValues,
            List<ProductVariantAttributeValue> savedVariantAttributeValues,
            Set<Long> processedOptionValueIds,
            Set<Long> processedAttributeValueIds
    ) {
        List<ProductOptionCombinationUpdateRequest> optionRequests = Objects.isNull(request.options())
                ? List.of()
                : request.options();

        Map<Long, ProductOptionValue> povById = Objects.isNull(productOptionValues)
                ? Map.of()
                : productOptionValues.stream()
                        .filter(pov -> Objects.nonNull(pov.getId()))
                        .collect(Collectors.toMap(ProductOptionValue::getId, Function.identity(), (e1, e2) -> e1));

        Map<String, ProductOptionValue> povByOptionAndValue = mapOptionValuesByOptionAndValue(productOptionValues);

        Map<Long, ProductVariant> currentVariantById = Objects.isNull(currentVariants)
                ? Map.of()
                : currentVariants.stream()
                        .collect(Collectors.toMap(ProductVariant::getId, Function.identity()));

        Map<String, VariantOptionValue> existingOptionValueMap = Objects.isNull(currentOptionValues)
                ? Map.of()
                : currentOptionValues.stream()
                        .filter(vov -> Objects.nonNull(vov.getProductVariant()) && Objects.nonNull(vov.getProductOptionValue()))
                        .collect(Collectors.toMap(
                                vov -> vov.getProductVariant().getId() + "_" + vov.getProductOptionValue().getId(),
                                Function.identity(),
                                (e1, e2) -> e1
                        ));

        Map<String, ProductVariantAttributeValue> existingVariantAttrMap = Objects.isNull(currentVariantAttributes)
                ? Map.of()
                : currentVariantAttributes.stream()
                        .filter(vav -> Objects.nonNull(vav.getProductVariant()) && Objects.nonNull(vav.getProductAttribute()))
                        .collect(Collectors.toMap(
                                vav -> vav.getProductVariant().getId() + "_" + vav.getProductAttribute().getId(),
                                Function.identity(),
                                (e1, e2) -> e1
                        ));

        List<Long> allVariantAttributeIds = request.variants().stream()
                .filter(v -> Objects.nonNull(v.attributeValues()))
                .flatMap(v -> v.attributeValues().stream())
                .map(ProductVariantAttributeValueUpdateRequest::productAttributeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, ProductAttribute> productAttributeById = allVariantAttributeIds.isEmpty()
                ? Map.of()
                : findProductAttributes(allVariantAttributeIds);

        int variantIndex = 0;
        for (ProductVariantUpdateRequest variantRequest : request.variants()) {
            ProductVariant variant = resolveVariant(variantRequest, product, currentVariantById);
            ProductVariant savedVariant = productVariantRepository.save(variant);
            savedVariants.add(savedVariant);

            updateSingleVariantOptionValues(
                    savedVariant,
                    variantIndex,
                    optionRequests,
                    povById,
                    povByOptionAndValue,
                    existingOptionValueMap,
                    processedOptionValueIds,
                    savedOptionValues
            );

            updateSingleVariantAttributeValues(
                    savedVariant,
                    variantRequest.attributeValues(),
                    productAttributeById,
                    existingVariantAttrMap,
                    processedAttributeValueIds,
                    savedVariantAttributeValues
            );
            variantIndex++;
        }
    }

    // Helper: Processes attribute value mappings for a single variant during update
    private void updateSingleVariantAttributeValues(
            ProductVariant savedVariant,
            List<ProductVariantAttributeValueUpdateRequest> attributeRequests,
            Map<Long, ProductAttribute> productAttributeById,
            Map<String, ProductVariantAttributeValue> existingVariantAttrMap,
            Set<Long> processedAttributeValueIds,
            List<ProductVariantAttributeValue> savedVariantAttributeValues
    ) {
        if (Objects.isNull(attributeRequests) || attributeRequests.isEmpty()) {
            return;
        }

        List<ProductVariantAttributeValue> toSave = new ArrayList<>();
        for (ProductVariantAttributeValueUpdateRequest attrReq : attributeRequests) {
            if (Objects.isNull(attrReq) || Objects.isNull(attrReq.productAttributeId())) {
                continue;
            }
            String key = savedVariant.getId() != null ? savedVariant.getId() + "_" + attrReq.productAttributeId() : null;
            ProductVariantAttributeValue existingAttrVal = key != null ? existingVariantAttrMap.get(key) : null;

            if (Objects.nonNull(existingAttrVal)) {
                existingAttrVal.setValue(attrReq.value());
                processedAttributeValueIds.add(existingAttrVal.getId());
                toSave.add(existingAttrVal);
            } else {
                ProductVariantAttributeValue newAttrVal = ProductVariantAttributeValue.builder()
                        .productVariant(savedVariant)
                        .productAttribute(productAttributeById.get(attrReq.productAttributeId()))
                        .value(attrReq.value())
                        .build();
                toSave.add(newAttrVal);
            }
        }

        if (!toSave.isEmpty()) {
            List<ProductVariantAttributeValue> savedList = productVariantAttributeValueRepository.saveAll(toSave);
            for (ProductVariantAttributeValue saved : savedList) {
                if (Objects.nonNull(saved.getId())) {
                    processedAttributeValueIds.add(saved.getId());
                }
            }
            savedVariantAttributeValues.addAll(savedList);
        }
    }

    // Helper: Deletes variant attribute values that were omitted during update
    private void deleteOmittedVariantAttributeValues(
            Product product,
            List<ProductVariantAttributeValue> currentVariantAttributes,
            Set<Long> processedAttributeValueIds
    ) {
        List<ProductVariantAttributeValue> deletedAttributes = Objects.isNull(currentVariantAttributes)
                ? List.of()
                : currentVariantAttributes.stream()
                        .filter(vav -> !processedAttributeValueIds.contains(vav.getId()))
                        .toList();
        if (!deletedAttributes.isEmpty()) {
            List<Long> deletedIds = deletedAttributes.stream().map(ProductVariantAttributeValue::getId).toList();
            log.debug("Deleting {} omitted variant attribute values (IDs: {}) for product ID: {}", deletedAttributes.size(), deletedIds, product.getId());
            productVariantAttributeValueRepository.deleteAll(deletedAttributes);
        }
    }

    // Helper: Processes option value mappings for a single variant during update
    private void updateSingleVariantOptionValues(
            ProductVariant savedVariant,
            int variantIndex,
            List<ProductOptionCombinationUpdateRequest> optionRequests,
            Map<Long, ProductOptionValue> povById,
            Map<String, ProductOptionValue> povByOptionAndValue,
            Map<String, VariantOptionValue> existingOptionValueMap,
            Set<Long> processedOptionValueIds,
            List<VariantOptionValue> savedOptionValues
    ) {
        for (ProductOptionCombinationUpdateRequest optionReq : optionRequests) {
            if (Objects.isNull(optionReq.values()) || variantIndex >= optionReq.values().size()) {
                continue;
            }
            ProductOptionValueUpdateRequest optValReq = optionReq.values().get(variantIndex);
            if (Objects.isNull(optValReq) || isBlank(optValReq.value())) {
                continue;
            }
            ProductOptionValue pov = resolveOptionValue(optValReq, optionReq.productOptionId(), povById, povByOptionAndValue);
            if (Objects.isNull(pov)) {
                continue;
            }

            String vovKey = savedVariant.getId() != null ? savedVariant.getId() + "_" + pov.getId() : null;
            VariantOptionValue existingOptionValue = vovKey != null ? existingOptionValueMap.get(vovKey) : null;

            if (Objects.nonNull(existingOptionValue)) {
                processedOptionValueIds.add(existingOptionValue.getId());
                savedOptionValues.add(existingOptionValue);
            } else {
                VariantOptionValue optionValue = createVariantOptionValue(savedVariant, pov);
                VariantOptionValue savedVov = variantOptionValueRepository.save(optionValue);
                processedOptionValueIds.add(savedVov.getId());
                savedOptionValues.add(savedVov);
            }
        }
    }

    // Helper: Resolves ProductOptionValue by ID or composite key
    private ProductOptionValue resolveOptionValue(
            ProductOptionValueUpdateRequest optValReq,
            Long productOptionId,
            Map<Long, ProductOptionValue> povById,
            Map<String, ProductOptionValue> povByOptionAndValue
    ) {
        ProductOptionValue pov = null;
        if (Objects.nonNull(optValReq.id())) {
            pov = povById.get(optValReq.id());
        }
        if (Objects.isNull(pov)) {
            String key = productOptionId + "_" + optValReq.value();
            pov = povByOptionAndValue.get(key);
        }
        return pov;
    }

    // Helper: Deletes variant option values that were omitted during update
    private void deleteOmittedVariantOptionValues(
            Product product,
            List<VariantOptionValue> currentOptionValues,
            Set<Long> processedOptionValueIds
    ) {
        List<VariantOptionValue> deletedOptionValues = Objects.isNull(currentOptionValues)
                ? List.of()
                : currentOptionValues.stream()
                        .filter(vov -> !processedOptionValueIds.contains(vov.getId()))
                        .toList();
        if (!deletedOptionValues.isEmpty()) {
            List<Long> deletedVovIds = deletedOptionValues.stream().map(VariantOptionValue::getId).toList();
            log.debug("Deleting {} omitted variant option values (IDs: {}) for product ID: {}", deletedOptionValues.size(), deletedVovIds, product.getId());
            variantOptionValueRepository.deleteAll(deletedOptionValues);
        }
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

    // Helper: Fetches and validates ProductOption entities by IDs
    private Map<Long, ProductOption> findProductOptions(List<Long> optionIds) {
        log.debug("Fetching {} product options from DB", optionIds.size());
        Map<Long, ProductOption> productOptionById = productOptionRepository.findAllById(optionIds)
                .stream()
                .collect(Collectors.toMap(ProductOption::getId, Function.identity()));
        if (productOptionById.size() != optionIds.stream().distinct().count()) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_OPTION_NOT_FOUND);
        }
        return productOptionById;
    }

    // Helper: Maps ProductOptionValue list to a lookup map keyed by optionId + "_" + value
    private Map<String, ProductOptionValue> mapOptionValuesByOptionAndValue(List<ProductOptionValue> productOptionValues) {
        if (Objects.isNull(productOptionValues) || productOptionValues.isEmpty()) {
            return Map.of();
        }
        return productOptionValues.stream()
                .filter(pov -> Objects.nonNull(pov.getProductOptionCombination())
                        && Objects.nonNull(pov.getProductOptionCombination().getProductOption()))
                .collect(Collectors.toMap(
                        pov -> pov.getProductOptionCombination().getProductOption().getId() + "_" + pov.getValue(),
                        Function.identity(),
                        (e1, e2) -> e1
                ));
    }

    // Helper: Constructs a ProductOptionCombination entity
    private ProductOptionCombination createProductOptionCombination(Product product, ProductOption option, Integer position) {
        return ProductOptionCombination.builder()
                .id(new ProductOptionCombinationId(product.getId(), option.getId()))
                .product(product)
                .productOption(option)
                .position(position)
                .build();
    }

    // Helper: Constructs a ProductOptionValue entity
    private ProductOptionValue createProductOptionValue(String value, Integer position, ProductOptionCombination combination) {
        return ProductOptionValue.builder()
                .value(value)
                .position(position)
                .productOptionCombination(combination)
                .build();
    }

    // Helper: Constructs a ProductCategory entity
    private ProductCategory createProductCategory(Product product, Category category) {
        return ProductCategory.builder()
                .id(new ProductCategoryId(product.getId(), category.getId()))
                .product(product)
                .category(category)
                .build();
    }

    // Helper: Constructs a VariantOptionValue entity
    private VariantOptionValue createVariantOptionValue(ProductVariant productVariant, ProductOptionValue productOptionValue) {
        return VariantOptionValue.builder()
                .productVariant(productVariant)
                .productOptionValue(productOptionValue)
                .build();
    }

}
