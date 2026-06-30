package com.yas.system.catalog.internal.service.impl;

import com.yas.system.catalog.internal.dto.request.ProductRequest;
import com.yas.system.catalog.internal.dto.request.ProductVariantRequest;
import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.entity.ProductOption;
import com.yas.system.catalog.internal.entity.ProductVariant;
import com.yas.system.catalog.internal.entity.VariantOptionValue;
import com.yas.system.catalog.internal.dto.response.ProductResponse;
import com.yas.system.catalog.internal.dto.response.ProductOptionResponse;
import com.yas.system.catalog.internal.dto.response.ProductVariantResponse;
import com.yas.system.catalog.internal.repository.ProductOptionRepository;
import com.yas.system.catalog.internal.repository.ProductRepository;
import com.yas.system.catalog.internal.repository.ProductVariantRepository;
import com.yas.system.catalog.internal.repository.VariantOptionValueRepository;
import com.yas.system.catalog.internal.service.ProductService;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.exception.InvalidDataException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
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

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        validateProductRequest(request);

        Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setSlug(request.slug());
        Product savedProduct = productRepository.save(product);

        List<ProductOption> options = Arrays.stream(request.options())
                .map(optionRequest -> ProductOption.builder()
                        .name(optionRequest.name())
                        .position(optionRequest.position())
                        .values(Arrays.asList(optionRequest.values()))
                        .product(savedProduct)
                        .build())
                .toList();
        List<ProductOption> savedOptions = productOptionRepository.saveAll(options);
        Map<Integer, ProductOption> optionByPosition = savedOptions.stream()
                .collect(Collectors.toMap(ProductOption::getPosition, Function.identity()));

        List<ProductVariant> savedVariants = Arrays.stream(request.variants())
                .map(variantRequest -> productVariantRepository.save(ProductVariant.builder()
                        .sku(variantRequest.sku())
                        .price(variantRequest.price())
                        .quantity(variantRequest.quantity())
                        .product(savedProduct)
                        .build()))
                .toList();

        List<VariantOptionValue> variantOptionValues = buildVariantOptionValues(request.variants(), savedVariants, optionByPosition);
        variantOptionValueRepository.saveAll(variantOptionValues);

        return toResponse(savedProduct, savedOptions, savedVariants, request.variants());
    }

    @Override
    public ProductResponse updateProduct(ProductRequest request) {
        return null;
    }

    @Override
    public ProductResponse getById(Long id) {
        return null;
    }

    private void validateProductRequest(ProductRequest request) {
        if (Objects.isNull(request) || isBlank(request.name()) || isBlank(request.slug())) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
        if (productRepository.existsByName(request.name()) || productRepository.existsBySlug(request.slug())) {
            throw new InvalidDataException(ErrorCode.PRODUCT_ALREADY_EXISTS);
        }
        validateOptions(request);
        validateVariants(request);
    }

    private void validateOptions(ProductRequest request) {
        if (Objects.isNull(request.options()) || request.options().length == 0) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
        boolean invalidOption = Arrays.stream(request.options())
                .anyMatch(option -> Objects.isNull(option)
                        || isBlank(option.name())
                        || Objects.isNull(option.values())
                        || option.values().length == 0
                        || option.position() < 1
                        || option.position() > 3
                        || Arrays.stream(option.values()).anyMatch(this::isBlank));
        if (invalidOption || hasDuplicatePositions(request)) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
    }

    private void validateVariants(ProductRequest request) {
        if (Objects.isNull(request.variants()) || request.variants().length == 0) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
        boolean invalidVariant = Arrays.stream(request.variants())
                .anyMatch(variant -> Objects.isNull(variant)
                        || isBlank(variant.sku())
                        || Objects.isNull(variant.price())
                        || variant.price() < 0
                        || variant.quality() < 0);
        if (invalidVariant) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT);
        }
    }

    private boolean hasDuplicatePositions(ProductRequest request) {
        return Arrays.stream(request.options())
                .map(option -> option.position())
                .distinct()
                .count() != request.options().length;
    }

    private List<VariantOptionValue> buildVariantOptionValues(
            ProductVariantRequest[] variantRequests,
            List<ProductVariant> savedVariants,
            Map<Integer, ProductOption> optionByPosition
    ) {
        return java.util.stream.IntStream.range(0, variantRequests.length)
                .boxed()
                .flatMap(index -> buildVariantOptionValues(variantRequests[index], savedVariants.get(index), optionByPosition).stream())
                .toList();
    }

    private List<VariantOptionValue> buildVariantOptionValues(
            ProductVariantRequest variantRequest,
            ProductVariant variant,
            Map<Integer, ProductOption> optionByPosition
    ) {
        return Stream.of(
                        buildVariantOptionValue(variant.option1(), variant, optionByPosition.get(1)),
                        buildVariantOptionValue(variant.option2(), variant, optionByPosition.get(2)),
                        buildVariantOptionValue(variant.option3(), variant, optionByPosition.get(3))
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

    private ProductResponse toResponse(
            Product product,
            List<ProductOption> options,
            List<ProductVariant> variants,
            ProductVariantRequest[] variantRequests
    ) {
        ProductOptionResponse[] optionResponses = options.stream()
                .map(this::toOptionResponse)
                .toArray(ProductOptionResponse[]::new);
        ProductVariantResponse[] variantResponses = java.util.stream.IntStream.range(0, variants.size())
                .mapToObj(index -> toVariantResponse(variants.get(index), variantRequests[index]))
                .toArray(ProductVariantResponse[]::new);
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getSlug(),
                optionResponses,
                variantResponses
        );
    }

    private ProductOptionResponse toOptionResponse(ProductOption option) {
        return new ProductOptionResponse(
                option.getId(),
                option.getName(),
                option.getValues().toArray(String[]::new),
                option.getPosition()
        );
    }

    private ProductVariantResponse toVariantResponse(ProductVariant variant, ProductVariantRequest variantRequest) {
        return new ProductVariantResponse(
                variant.getId(),
                variantRequest.option1(),
                variantRequest.option2(),
                variantRequest.option3(),
                variant.getSku(),
                variant.getPrice(),
                variant.getQuantity()
        );
    }

    private boolean isBlank(String value) {
        return Objects.isNull(value) || value.isBlank();
    }
}
