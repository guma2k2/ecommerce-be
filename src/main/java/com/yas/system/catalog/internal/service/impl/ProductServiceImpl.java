package com.yas.system.catalog.internal.service.impl;

import com.yas.system.catalog.internal.dto.request.ProductOptionRequest;
import com.yas.system.catalog.internal.dto.request.ProductRequest;
import com.yas.system.catalog.internal.dto.request.ProductVariantRequest;
import com.yas.system.catalog.internal.dto.response.ProductResponse;
import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.entity.ProductOption;
import com.yas.system.catalog.internal.entity.ProductVariant;
import com.yas.system.catalog.internal.entity.VariantOptionValue;
import com.yas.system.catalog.internal.helper.ProductHelper;
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

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductServiceImpl implements ProductService {

    ProductRepository productRepository;
    ProductVariantRepository productVariantRepository;
    ProductOptionRepository productOptionRepository;
    VariantOptionValueRepository variantOptionValueRepository;
    ProductHelper productHelper;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        validateProductRequest(request);

        Product savedProduct = productRepository.save(productHelper.createProduct(request));
        List<ProductOption> savedOptions = saveOptions(request, savedProduct);
        List<ProductVariant> savedVariants = saveVariants(request, savedProduct);
        List<VariantOptionValue> savedVariantOptionValues = saveVariantOptionValues(
                request,
                savedOptions,
                savedVariants
        );

        return ProductResponse.from(savedProduct, savedOptions, savedVariants, savedVariantOptionValues);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(ProductRequest request, Long productId) {
        validateProductRequest(request, productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        productHelper.updateProduct(request, product);
        Product savedProduct = productRepository.save(product);

        List<ProductOption> currentOptions = productOptionRepository.findByProductId(productId);
        List<ProductVariant> currentVariants = productVariantRepository.findByProductId(productId);
        variantOptionValueRepository.deleteByProductId(productId);

        List<ProductOption> savedOptions = saveUpdatedOptions(request, savedProduct, currentOptions);
        List<ProductVariant> savedVariants = saveUpdatedVariants(request, savedProduct, currentVariants);
        List<VariantOptionValue> savedVariantOptionValues = saveVariantOptionValues(
                request,
                savedOptions,
                savedVariants
        );

        return ProductResponse.from(savedProduct, savedOptions, savedVariants, savedVariantOptionValues);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        if (Objects.isNull(id)) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        List<ProductOption> options = productOptionRepository.findByProductId(id);
        List<ProductVariant> variants = productVariantRepository.findByProductId(id);
        List<VariantOptionValue> variantOptionValues = variantOptionValueRepository.findByProductVariantProductId(id);

        return ProductResponse.from(product, options, variants, variantOptionValues);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (Objects.isNull(id)) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        variantOptionValueRepository.deleteByProductId(id);
        productOptionRepository.deleteByProductId(id);
        productVariantRepository.deleteByProductId(id);
        productRepository.delete(product);
    }

    private List<ProductOption> saveOptions(ProductRequest request, Product product) {
        List<ProductOption> options = Stream.of(request.options())
                .map(optionRequest -> optionRequest.toEntity(product))
                .toList();
        return productOptionRepository.saveAll(options);
    }

    private List<ProductVariant> saveVariants(ProductRequest request, Product product) {
        return Stream.of(request.variants())
                .map(variantRequest -> productVariantRepository.save(variantRequest.toEntity(product)))
                .toList();
    }

    private List<VariantOptionValue> saveVariantOptionValues(
            ProductRequest request,
            List<ProductOption> options,
            List<ProductVariant> variants
    ) {
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
        validateOptions(request);
        validateVariants(request);
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
        validateOptions(request);
        validateVariants(request);
    }

    private void validateOptions(ProductRequest request) {
        if (Objects.isNull(request.options()) || request.options().length == 0) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
        boolean invalidOption = Stream.of(request.options())
                .anyMatch(option -> Objects.isNull(option)
                        || isBlank(option.name())
                        || Objects.isNull(option.values())
                        || option.values().length == 0
                        || option.position() < 1
                        || option.position() > 3
                        || Stream.of(option.values()).anyMatch(this::isBlank));
        if (invalidOption || hasDuplicatePositions(request)) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
    }

    private void validateVariants(ProductRequest request) {
        if (Objects.isNull(request.variants()) || request.variants().length == 0) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
        boolean invalidVariant = Stream.of(request.variants())
                .anyMatch(variant -> Objects.isNull(variant)
                        || isBlank(variant.sku())
                        || Objects.isNull(variant.price())
                        || variant.price() < 0
                        || variant.quantity() < 0);
        if (invalidVariant) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
    }

    private boolean hasDuplicatePositions(ProductRequest request) {
        return Stream.of(request.options())
                .map(ProductOptionRequest::position)
                .distinct()
                .count() != request.options().length;
    }

    private List<VariantOptionValue> buildVariantOptionValues(
            ProductVariantRequest[] variantRequests,
            List<ProductVariant> savedVariants,
            Map<Integer, ProductOption> optionByPosition
    ) {
        return IntStream.range(0, variantRequests.length)
                .boxed()
                .flatMap(index -> buildVariantOptionValues(
                        variantRequests[index],
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
        List<ProductOption> options = Stream.of(request.options())
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
        List<Long> requestIds = Stream.of(request.options())
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
        List<ProductVariant> variants = Stream.of(request.variants())
                .map(variantRequest -> resolveVariant(variantRequest, product, currentVariantById))
                .toList();
        deleteMissingVariants(request, currentVariants);
        return productVariantRepository.saveAll(variants);
    }

    private ProductVariant resolveVariant(
            ProductVariantRequest request,
            Product product,
            Map<Long, ProductVariant> currentVariantById
    ) {
        if (Objects.isNull(request.id())) {
            return request.toEntity(product);
        }
        ProductVariant variant = currentVariantById.get(request.id());
        if (Objects.isNull(variant)) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
        request.applyTo(variant);
        return variant;
    }

    private void deleteMissingVariants(ProductRequest request, List<ProductVariant> currentVariants) {
        List<Long> requestIds = Stream.of(request.variants())
                .map(ProductVariantRequest::id)
                .filter(Objects::nonNull)
                .toList();
        List<ProductVariant> deletedVariants = currentVariants.stream()
                .filter(variant -> !requestIds.contains(variant.getId()))
                .toList();
        productVariantRepository.deleteAll(deletedVariants);
    }

    private boolean isBlank(String value) {
        return Objects.isNull(value) || value.isBlank();
    }
}
