package com.mak.springbootefficientsearchapi.entity.utils;

import com.mak.springbootefficientsearchapi.entity.Car;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DTO using for pagination
 */
@Getter
@Setter
@AllArgsConstructor
public class PagingResponse {

    /**
     * entity count
     */
    private Long count;
    /**
     * page number, 0 indicate the first page.
     */
    private Long pageNumber;
    /**
     * size of page, 0 indicate infinite-sized.
     */
    private Long pageSize;
    /**
     * Offset from the of pagination.
     */
    private Long pageOffset;
    /**
     * the number total of pages.
     */
    private Long pageTotal;
    /**
     * elements of page.
     */
    private List<Car> elements;
}
