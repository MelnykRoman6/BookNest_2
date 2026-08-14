package org.example.booknest_2.util;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

public final class OpenLibraryParser {

    private OpenLibraryParser() {}

    /**
     * OpenLibrary returns a description either as a plain string or as an object
     * holding the text under "value".
     */
    public static String extractDescription(Object rawDescription) {
        if (rawDescription instanceof String description) {
            return description;
        }
        if (rawDescription instanceof Map<?, ?> descriptionMap) {
            Object value = descriptionMap.get("value");
            return value != null ? value.toString() : null;
        }
        return null;
    }

    public static String extractFirstCoverId(Object rawCovers) {
        if (rawCovers instanceof List<?> covers && !covers.isEmpty() && covers.get(0) != null) {
            return covers.get(0).toString();
        }
        return null;
    }

    public static String extractIaId(JsonNode node) {
        String ocaid = textValue(node, "ocaid");
        if (ocaid != null) {
            return ocaid;
        }
        String ia = firstArrayValue(node, "ia");
        if (ia != null) {
            return ia;
        }
        return firstArrayValue(node, "ia_box_id");
    }

    private static String textValue(JsonNode node, String field) {
        if (node.has(field) && !node.get(field).asText().trim().isEmpty()) {
            return node.get(field).asText().trim();
        }
        return null;
    }

    private static String firstArrayValue(JsonNode node, String field) {
        if (node.has(field) && node.get(field).isArray() && !node.get(field).isEmpty()) {
            return node.get(field).get(0).asText().trim();
        }
        return null;
    }
}
