package ru.practicum.ewm.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Offset-based pagination helper for converting (from, size) into a Spring {@link Pageable}.
 */
public final class PageUtil {

    private PageUtil() {
    }

    /**
     * Builds {@link Pageable} from offset/limit parameters.
     *
     * @param from non-negative offset (>= 0)
     * @param size positive page size (> 0)
     * @param sort optional sort (can be null)
     * @return {@link Pageable} instance
     * @throws IllegalArgumentException if parameters are invalid
     */
    public static Pageable byFromSize(int from, int size, Sort sort) {
        if (from < 0) {
            throw new IllegalArgumentException("Parameter 'from' must be >= 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Parameter 'size' must be > 0");
        }
        int page = from / size;
        return (sort == null)
                ? PageRequest.of(page, size)
                : PageRequest.of(page, size, sort);
    }
}