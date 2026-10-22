package org.example.booknest_2.util;

import java.util.regex.Pattern;

/**
 * Validation of identifiers that are interpolated into third party URLs.
 */
public final class ExternalIds {

    private static final Pattern INTERNET_ARCHIVE_ID = Pattern.compile("^[A-Za-z0-9._-]{1,120}$");
    private static final Pattern OPEN_LIBRARY_ID = Pattern.compile("^[A-Za-z0-9]{1,40}$");

    private ExternalIds() {
    }

    public static boolean isValidInternetArchiveId(String id) {
        return id != null && INTERNET_ARCHIVE_ID.matcher(id).matches();
    }

    public static boolean isValidOpenLibraryId(String id) {
        return id != null && OPEN_LIBRARY_ID.matcher(id).matches();
    }
}
