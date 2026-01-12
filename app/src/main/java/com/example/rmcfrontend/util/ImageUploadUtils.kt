package com.example.rmcfrontend.util

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Converts Android content Uris to OkHttp multipart parts.
 * The Ktor backend accepts any PartData.FileItem parts; the part name is irrelevant.
 */
object ImageUploadUtils {

    fun urisToMultipartParts(
        context: Context,
        uris: List<Uri>,
        partName: String = "image"
    ): List<MultipartBody.Part> {
        return uris.mapNotNull { uri ->
            uriToMultipartPart(context, uri, partName)
        }
    }

    private fun uriToMultipartPart(
        context: Context,
        uri: Uri,
        partName: String
    ): MultipartBody.Part? {
        val cr = context.contentResolver
        val mime = cr.getType(uri) ?: "image/*"
        val fileName = cr.queryDisplayName(uri) ?: "image.jpg"

        val bytes = cr.openInputStream(uri)?.use { it.readBytes() } ?: return null
        val body = bytes.toRequestBody(mime.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, fileName, body)
    }

    private fun ContentResolver.queryDisplayName(uri: Uri): String? {
        var cursor: Cursor? = null
        return try {
            cursor = query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) cursor.getString(idx) else null
            } else null
        } catch (_: Exception) {
            null
        } finally {
            cursor?.close()
        }
    }
}
