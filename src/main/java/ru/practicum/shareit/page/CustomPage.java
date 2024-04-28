package ru.practicum.shareit.page;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class CustomPage extends PageRequest {
    private final int from;
    private final int size;
    private final Sort sort;

    public CustomPage(int from, int size, Sort sort) {
        super(from, size, sort);
        this.from = from;
        this.size = size;
        this.sort = sort;
    }

    @Override
    public int getPageNumber() {
        return 0;
    }

    @Override
    public int getPageSize() {
        return size;
    }

    @Override
    public long getOffset() {
        return from;
    }
}
