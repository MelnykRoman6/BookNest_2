package org.example.booknest_2.util;

import java.util.function.Supplier;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String entityName, Object id) {
        super(entityName + " not found: " + id);
    }

    public static Supplier<NotFoundException> of(String entityName, Object id) {
        return () -> new NotFoundException(entityName, id);
    }
}
