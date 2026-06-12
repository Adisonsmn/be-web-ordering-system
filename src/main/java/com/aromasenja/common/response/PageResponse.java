package com.aromasenja.common.response;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Wrapper pagination untuk response list.
 * Konversi dari Spring Data Page<T> ke format JSON yang konsisten.
 * Format: { content, page, size, totalElements, totalPages }
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    /**
     * Factory method — konversi Spring Data Page ke PageResponse.
     *
     * @param page hasil query dari repository
     * @return PageResponse yang siap di-return dari controller
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
