package com.yas.system.catalog.internal.service.impl;

import com.yas.system.catalog.internal.dto.request.ProductAttributeValueRequest;
import com.yas.system.catalog.internal.dto.request.ProductOptionRequest;
import com.yas.system.catalog.internal.dto.request.ProductRequest;
import com.yas.system.catalog.internal.dto.request.ProductVariantRequest;
import com.yas.system.catalog.internal.dto.response.ProductResponse;
import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.entity.ProductOption;
import com.yas.system.catalog.internal.entity.ProductVariant;
import com.yas.system.catalog.internal.entity.VariantOptionValue;
import com.yas.system.catalog.internal.entity.attribute.ProductAttribute;
import com.yas.system.catalog.internal.entity.attribute.ProductAttributeValue;
import com.yas.system.catalog.internal.helper.ProductHelper;
import com.yas.system.catalog.internal.repository.ProductAttributeRepository;
import com.yas.system.catalog.internal.repository.ProductAttributeValueRepository;
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

import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    ProductHelper productHelper;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        validateProductRequest(request);

        Product savedProduct = productRepository.save(productHelper.createProduct(request));
        List<ProductAttributeValue> savedAttributes = saveProductAttributeValues(request, savedProduct);
        List<ProductOption> savedOptions = saveOptions(request, savedProduct);
        List<ProductVariant> savedVariants = saveVariants(request, savedProduct);
        List<VariantOptionValue> savedVariantOptionValues = saveVariantOptionValues(
                request,
                savedOptions,
                savedVariants
        );

        return ProductResponse.from(savedProduct, savedAttributes, savedOptions, savedVariants, savedVariantOptionValues);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(ProductRequest request, Long productId) {
        validateProductRequest(request, productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        productHelper.updateProduct(request, product);
        Product savedProduct = productRepository.save(product);

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
        List<VariantOptionValue> savedVariantOptionValues = saveVariantOptionValues(
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

        productAttributeValueRepository.deleteByProductId(id);
        variantOptionValueRepository.deleteByProductId(id);
        productOptionRepository.deleteByProductId(id);
        productVariantRepository.deleteByProductId(id);
        productRepository.delete(product);
    }

    private List<ProductAttributeValue> saveProductAttributeValues(ProductRequest request, Product product) {
        if (Objects.isNull(request.attributes()) || request.attributes().isEmpty()) {
            return List.of();
        }
        Map<Long, ProductAttribute> productAttributeById = findProductAttributes(request);
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
            ProductRequest request,
            Product product,
            List<ProductAttributeValue> currentAttributes
    ) {
        if (Objects.isNull(request.attributes()) || request.attributes().isEmpty()) {
            productAttributeValueRepository.deleteAll(currentAttributes);
            return List.of();
        }
        Map<Long, ProductAttribute> productAttributeById = findProductAttributes(request);
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

    private Map<Long, ProductAttribute> findProductAttributes(ProductRequest request) {
        List<Long> productAttributeIds = request.attributes().stream()
                .map(ProductAttributeValueRequest::productAttributeId)
                .toList();
        Map<Long, ProductAttribute> productAttributeById = productAttributeRepository.findAllById(productAttributeIds)
                .stream()
                .collect(Collectors.toMap(ProductAttribute::getId, Function.identity()));
        if (productAttributeById.size() != productAttributeIds.stream().distinct().count()) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_ATTRIBUTE_NOT_FOUND);
        }
        return productAttributeById;
    }

    private ProductAttributeValue resolveProductAttributeValue(
            ProductAttributeValueRequest request,
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
            ProductRequest request,
            List<ProductAttributeValue> currentAttributes
    ) {
        List<Long> requestProductAttributeIds = request.attributes().stream()
                .map(ProductAttributeValueRequest::productAttributeId)
                .toList();
        List<ProductAttributeValue> deletedAttributes = currentAttributes.stream()
                .filter(attributeValue -> !requestProductAttributeIds.contains(attributeValue.getProductAttribute().getId()))
                .toList();
        productAttributeValueRepository.deleteAll(deletedAttributes);
    }

    private List<ProductOption> saveOptions(ProductRequest request, Product product) {
        List<ProductOption> options = request.options().stream()
                .map(optionRequest -> optionRequest.toEntity(product))
                .toList();
        return productOptionRepository.saveAll(options);
    }

    private List<ProductVariant> saveVariants(ProductRequest request, Product product) {
        boolean hasOptions = hasOptions(request);
        return request.variants().stream()
                .map(variantRequest -> {
                    ProductVariant variant = variantRequest.toEntity(product);
                    applyDefaultVariantTitle(variant, hasOptions);
                    return productVariantRepository.save(variant);
                })
                .toList();
    }

    private List<VariantOptionValue> saveVariantOptionValues(
            ProductRequest request,
            List<ProductOption> options,
            List<ProductVariant> variants
    ) {
        if (!hasOptions(request)) {
            return List.of();
        }
        Map<Integer, ProductOption> optionByPosition = options.stream()
                .collect(Collectors.toMap(ProductOption::getPosition, Function.identity()));
        List<VariantOptionValue> variantOptionValues = buildVariantOptionValues(
                request.variants(),
                variants,
                optionByPosition
        );
        return variantOptionValueRepository.saveAll(variantOptionValues);
    }

    private void validateProductRequest(ProductRequest request) {
        if (Objects.isNull(request)) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
        if (isBlank(request.name()) || isBlank(request.slug())) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
        if (productRepository.existsByName(request.name()) || productRepository.existsBySlug(request.slug())) {
            throw new InvalidDataException(ErrorCode.PRODUCT_ALREADY_EXISTS);
        }
        validateAttributes(request);
        validateOptions(request);
        validateVariants(request);
        validateCreateIds(request);
    }

    private void validateProductRequest(ProductRequest request, Long productId) {
        if (Objects.isNull(productId)) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
        if (Objects.isNull(request) || isBlank(request.name()) || isBlank(request.slug())) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
        if (productRepository.existsByNameAndIdNot(request.name(), productId)
                || productRepository.existsBySlugAndIdNot(request.slug(), productId)) {
            throw new InvalidDataException(ErrorCode.PRODUCT_ALREADY_EXISTS);
        }
        validateAttributes(request);
        validateOptions(request);
        validateVariants(request);
        validateUpdateIds(request);
    }

    private void validateCreateIds(ProductRequest request) {
        boolean hasRequestId = request.options().stream().anyMatch(option -> Objects.nonNull(option.id()))
                || request.variants().stream().anyMatch(variant -> Objects.nonNull(variant.id()));
        if (hasRequestId) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
    }

    private void validateUpdateIds(ProductRequest request) {
        boolean hasDuplicateOptionId = hasDuplicateIds(request.options().stream()
                .map(ProductOptionRequest::id)
                .filter(Objects::nonNull)
                .toList());
        boolean hasDuplicateVariantId = hasDuplicateIds(request.variants().stream()
                .map(ProductVariantRequest::id)
                .filter(Objects::nonNull)
                .toList());
        if (hasDuplicateOptionId || hasDuplicateVariantId) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
    }

    private boolean hasDuplicateIds(List<Long> ids) {
        return ids.stream().distinct().count() != ids.size();
    }

    private void validateOptions(ProductRequest request) {
        if (Objects.isNull(request.options())) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
        if (request.options().isEmpty()) {
            return;
        }
        boolean invalidOption = request.options().stream()
                .anyMatch(option -> Objects.isNull(option)
                        || isBlank(option.name())
                        || Objects.isNull(option.values())
                        || option.values().isEmpty()
                        || option.position() < 1
                        || option.position() > 3
                        || option.values().stream().anyMatch(value -> isBlank(value)));
        if (invalidOption || hasDuplicatePositions(request)) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
    }

    private void validateVariants(ProductRequest request) {
        if (Objects.isNull(request.variants()) || request.variants().isEmpty()) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
        boolean invalidVariant = request.variants().stream()
                .anyMatch(variant -> Objects.isNull(variant)
                        || isBlank(variant.sku())
                        || Objects.isNull(variant.price())
                        || variant.price() < 0
                        || variant.quantity() < 0);
        if (invalidVariant) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
    }

    private void validateAttributes(ProductRequest request) {
        if (Objects.isNull(request.attributes()) || request.attributes().isEmpty()) {
            return;
        }
        boolean invalidAttribute = request.attributes().stream()
                .anyMatch(attribute -> Objects.isNull(attribute)
                        || Objects.isNull(attribute.productAttributeId())
                        || isBlank(attribute.value()));
        if (invalidAttribute) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
        boolean hasDuplicateAttribute = request.attributes().stream()
                .map(attribute -> attribute.productAttributeId())
                .distinct()
                .count() != request.attributes().size();
        if (hasDuplicateAttribute) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
    }

    private boolean hasDuplicatePositions(ProductRequest request) {
        return request.options().stream()
                .map(ProductOptionRequest::position)
                .distinct()
                .count() != request.options().size();
    }

    private List<VariantOptionValue> buildVariantOptionValues(
            List<ProductVariantRequest> variantRequests,
            List<ProductVariant> savedVariants,
            Map<Integer, ProductOption> optionByPosition
    ) {
        return IntStream.range(0, variantRequests.size())
                .boxed()
                .flatMap(index -> buildVariantOptionValues(
                        variantRequests.get(index),
                        savedVariants.get(index),
                        optionByPosition
                ).stream())
                .toList();
    }

    private List<VariantOptionValue> buildVariantOptionValues(
            ProductVariantRequest variantRequest,
            ProductVariant variant,
            Map<Integer, ProductOption> optionByPosition
    ) {
        return Stream.of(
                        buildVariantOptionValue(variantRequest.option1(), variant, optionByPosition.get(1)),
                        buildVariantOptionValue(variantRequest.option2(), variant, optionByPosition.get(2)),
                        buildVariantOptionValue(variantRequest.option3(), variant, optionByPosition.get(3))
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
            ProductRequest request,
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
            ProductOptionRequest request,
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

    private void deleteMissingOptions(ProductRequest request, List<ProductOption> currentOptions) {
        List<Long> requestIds = request.options().stream()
                .map(ProductOptionRequest::id)
                .filter(Objects::nonNull)
                .toList();
        List<ProductOption> deletedOptions = currentOptions.stream()
                .filter(option -> !requestIds.contains(option.getId()))
                .toList();
        productOptionRepository.deleteAll(deletedOptions);
    }

    private List<ProductVariant> saveUpdatedVariants(
            ProductRequest request,
            Product product,
            List<ProductVariant> currentVariants
    ) {
        Map<Long, ProductVariant> currentVariantById = currentVariants.stream()
                .collect(Collectors.toMap(ProductVariant::getId, Function.identity()));
        boolean hasOptions = hasOptions(request);
        List<ProductVariant> variants = request.variants().stream()
                .map(variantRequest -> resolveVariant(variantRequest, product, currentVariantById, hasOptions))
                .toList();
        deleteMissingVariants(request, currentVariants);
        return productVariantRepository.saveAll(variants);
    }

    private ProductVariant resolveVariant(
            ProductVariantRequest request,
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

    private boolean hasOptions(ProductRequest request) {
        return Objects.nonNull(request.options()) && !request.options().isEmpty();
    }

    private void deleteMissingVariants(ProductRequest request, List<ProductVariant> currentVariants) {
        List<Long> requestIds = request.variants().stream()
                .map(ProductVariantRequest::id)
                .filter(Objects::nonNull)
                .toList();
        List<ProductVariant> deletedVariants = currentVariants.stream()
                .filter(variant -> !requestIds.contains(variant.getId()))
                .toList();
        productVariantRepository.deleteAll(deletedVariants);
    }

}
