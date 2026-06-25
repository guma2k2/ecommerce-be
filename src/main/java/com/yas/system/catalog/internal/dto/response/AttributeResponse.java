package com.yas.system.catalog.internal.dto.response;

import com.tiki.product.entity.Attribute;

public record AttributeResponse (
        Integer id,
        String name,
        String unit,
        String dataType
) {

    public static AttributeResponse from(Attribute attribute) {
        return new AttributeResponse(attribute.getId(), attribute.getName(), attribute.getUnit(), attribute.getDataType());
    }
}
