package com.yas.system.catalog.internal.helper;

import com.yas.system.catalog.internal.dto.request.ProductOptionCombinationCreateRequest;
import com.yas.system.catalog.internal.dto.request.ProductOptionValueCreateRequest;
import com.yas.system.catalog.internal.dto.response.ProductOptionCombinationResponse;
import com.yas.system.catalog.internal.dto.response.ProductOptionValueResponse;
import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.entity.option.ProductOption;
import com.yas.system.catalog.internal.entity.option.ProductOptionCombination;
import com.yas.system.catalog.internal.entity.option.ProductOptionCombinationId;
import com.yas.system.catalog.internal.entity.option.ProductOptionValue;
import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.yas.system.common.util.StringUtils.isBlank;

@Component
public class ProductOptionCombinationHelper {

    public ProductOptionCombination createProductOptionCombination(
            Product product,
            ProductOption option,
            Integer position
    ) {
        if (Objects.isNull(option) || Objects.isNull(option.getId())) {
            throw new ApplicationException(ErrorCode.PRODUCT_OPTION_NOT_FOUND);
        }
        return ProductOptionCombination.builder()
                .id(new ProductOptionCombinationId(product.getId(), option.getId()))
                .product(product)
                .productOption(option)
                .position(position)
                .build();
    }

    public ProductOptionValue createProductOptionValue(
            String value,
            Integer position,
            ProductOptionCombination combination
    ) {
        return ProductOptionValue.builder()
                .value(value)
                .position(position)
                .productOptionCombination(combination)
                .build();
    }

    public List<ProductOptionValue> buildProductOptionValuesToSave(
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

    public Map<String, ProductOptionValue> mapOptionValuesByOptionAndValue(List<ProductOptionValue> productOptionValues) {
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

    public List<ProductOptionCombinationResponse> buildOptionCombinationResponses(
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
                                )));

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
}
