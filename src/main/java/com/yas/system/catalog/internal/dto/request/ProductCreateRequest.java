package com.yas.system.catalog.internal.dto.request;

import com.yas.system.catalog.api.enumeration.ProductStatus;
import com.yas.system.common.response.ParamError;
import com.yas.system.common.validation.annotation.ValidateEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProductCreateRequest(
        @NotBlank(message = ParamError.FIELD_NAME)
        @Size(max = 100, message = ParamError.MAX_LENGTH)
        String name,

        String description,

        @NotBlank(message = ParamError.FIELD_NAME)
        @Size(max = 100, message = ParamError.MAX_LENGTH)
        String slug,

        @Size(max = 60, message = ParamError.MAX_LENGTH)
        String metaTitle,

        @Size(max = 200, message = ParamError.MAX_LENGTH)
        String metaKeyword,

        @Size(max = 160, message = ParamError.MAX_LENGTH)
        String metaDescription,

        @NotBlank(message = ParamError.FIELD_NAME)
        Integer categoryId,

        @NotBlank(message = ParamError.FIELD_NAME)
        Integer brandId,

        @NotBlank(message = ParamError.FIELD_NAME)
        @ValidateEnum(enumClass = ProductStatus.class, message = "Invalid product status")
        String status,

        @Valid
        List<ProductMediaRequest> medias,

        @Valid
        List<ProductOptionCombinationCreateRequest> options,

        @Valid
        List<ProductAttributeValueCreateRequest> attributes,

        @Valid
        @NotEmpty(message = ParamError.FIELD_NAME)
        List<ProductVariantCreateRequest> variants
) {
}
