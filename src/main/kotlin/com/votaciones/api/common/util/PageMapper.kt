package com.votaciones.api.common.util

import com.votaciones.api.common.dto.PageResponse
import org.springframework.data.domain.Page

object PageMapper {

    fun <T : Any> from(page: Page<T>): PageResponse<T> = PageResponse(
        content = page.content,
        page = page.number,
        size = page.size,
        totalElements = page.totalElements,
        totalPages = page.totalPages,
        last = page.isLast,
    )
}
