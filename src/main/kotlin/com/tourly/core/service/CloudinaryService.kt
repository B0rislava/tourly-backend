package com.tourly.core.service

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.cloudinary.Transformation
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class CloudinaryService(
    private val cloudinary: Cloudinary
) {

    companion object {
        private const val BASE_FOLDER = "Tourly"
    }

    fun uploadImage(file: MultipartFile, folder: String, publicId: String): String {
        validateFile(file)
        
        val uploadResult: Map<String, Any>

        @Suppress("UNCHECKED_CAST")
        try {
            val transformation = Transformation<Transformation<*>>()
                .width(1024)
                .crop("limit")
                .quality("auto")
                .fetchFormat("auto")

            uploadResult = cloudinary.uploader().upload(
                file.bytes, ObjectUtils.asMap(
                    "folder", "$BASE_FOLDER/$folder",
                    "public_id", publicId,
                    "overwrite", true,
                    "resource_type", "image",
                    "transformation", transformation
                )
            ) as Map<String, Any>
        } catch (e: Exception) {
            throw RuntimeException("Failed to upload image", e)
        }

        return uploadResult["secure_url"] as String
    }

    private fun validateFile(file: MultipartFile) {


        // 2. Validate MIME type
        val contentType = file.contentType ?: throw IllegalArgumentException("Invalid file type")
        if (!contentType.startsWith("image/")) {
            throw IllegalArgumentException("Only image files are allowed")
        }

    }
}
