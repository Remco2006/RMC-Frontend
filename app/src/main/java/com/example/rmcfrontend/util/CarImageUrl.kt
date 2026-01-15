package com.example.rmcfrontend.util

import com.example.rmcfrontend.api.ApiClient

/**
 * Returns a displayable URL for a car image.
 *
 * The backend typically returns just a filename (uuid.jpg). However, depending on how the
 * server constructs responses, you may also encounter full URLs in the DB/response such as:
 *   - http://localhost:8080/uploads/cars/<uuid>.jpg
 *   - http://localhost:8080/uploads/cars/http://localhost:8080/uploads/cars/<uuid>.jpg
 */
fun carImageUrl(carId: Long?, rawValue: String): String {
    // Based on your screenshots/DB, images are served under:
    //   /uploads/cars/<fileName>
    val base = ApiClient.BASE_URL.trimEnd('/')
    val normalized = normalizeRawImageValue(rawValue)
    if (normalized.isBlank()) return ""

    return when {
        // If the backend stored a full path under /uploads/... keep that path, but swap host.
        normalized.startsWith("/uploads/") -> "$base$normalized"

        // Otherwise assume it's just a filename.
        else -> "$base/uploads/cars/$normalized"
    }
}

private fun normalizeRawImageValue(rawValue: String): String {
    var s = rawValue.trim()
    if (s.isBlank()) return ""

    // Strip query / fragment
    s = s.substringBefore('?').substringBefore('#')

    // If it contains '/uploads/', take that path segment (handles duplicated URLs too).
    // Example:
    //  http://localhost:8080/uploads/cars/http://localhost:8080/uploads/cars/a.jpg
    // becomes:
    //  /uploads/cars/a.jpg
    val uploadsIndex = s.lastIndexOf("/uploads/")
    if (uploadsIndex >= 0) {
        s = s.substring(uploadsIndex)
        // Now ensure we keep only one occurrence of '/uploads/' and the final file name
        // (in case the string still contains another '/uploads/' inside).
        val innerIndex = s.lastIndexOf("/uploads/")
        if (innerIndex > 0) s = s.substring(innerIndex)
        return s
    }

    // Otherwise: if it's a URL/path, keep only the last segment (filename).
    if (s.contains('/')) s = s.substringAfterLast('/')
    return s
}
