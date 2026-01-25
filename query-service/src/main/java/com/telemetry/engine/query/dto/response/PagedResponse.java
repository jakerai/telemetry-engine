package com.telemetry.engine.query.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record PagedResponse<T>(List<T> data, int page, int size, long totalElements,
    int totalPages, boolean hasNext) {

}
