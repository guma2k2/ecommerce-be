package com.yas.system.common.response;

import java.util.List;

public record PageResponse <T> (
        int pageNumber,
        int pageSize,
        int totalPages,
        long totalElements,
        List<T> content
) {
}
