package org.example.booknest_2.util;

public final class InternetArchiveUrls {

    private static final String BASE = "https://archive.org";

    private InternetArchiveUrls() {}

    public static String pdf(String iaId) {
        return BASE + "/download/" + iaId + "/" + iaId + ".pdf";
    }

    public static String fullText(String iaId) {
        return BASE + "/stream/" + iaId + "/" + iaId + "_djvu.txt";
    }
}
