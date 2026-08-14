package org.example.booknest_2.util;

public final class OpenLibraryUrls {

    private static final String BASE = "https://openlibrary.org";
    private static final String COVERS_BASE = "https://covers.openlibrary.org/b/id/";
    public static final String PLACEHOLDER_COVER_URL = "https://via.placeholder.com/100x150?text=No+Cover";

    private OpenLibraryUrls() {}

    public static String search(String query) {
        return BASE + "/search.json?q=" + query.replace(" ", "+");
    }

    public static String work(String openLibraryId) {
        return BASE + "/works/" + openLibraryId + ".json";
    }

    public static String editions(String openLibraryId) {
        return BASE + "/works/" + openLibraryId + "/editions.json";
    }

    public static String cover(String coverId) {
        return (coverId != null && !coverId.isEmpty())
                ? COVERS_BASE + coverId + "-M.jpg"
                : PLACEHOLDER_COVER_URL;
    }

    public static String workIdFromKey(String key) {
        return key != null ? key.replace("/works/", "") : "";
    }

    public static String authorIdFromKey(String key) {
        return key != null ? key.replace("/authors/", "") : "";
    }
}
