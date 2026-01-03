package com.tourly.core.service

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class CloudinaryService(
    private val cloudinary: Cloudinary
) {

    fun uploadImage(file: MultipartFile, userId: Long): String {
        val imageBytes = if (file.size > 2 * 1024 * 1024) {
            compressImage(file)
        } else {
            validateFile(file)
            file.bytes
        }

        val uploadResult: Map<String, Any>

        @Suppress("UNCHECKED_CAST")
        try {
            uploadResult = cloudinary.uploader().upload(
                imageBytes, ObjectUtils.asMap(
                    "folder", "avatars",
                    "public_id", "user_${userId}",
                    "overwrite", true,
                    "resource_type", "image"
                )
            ) as Map<String, Any>
        } catch (e: Exception) {
            throw RuntimeException("Failed to upload image", e)
        }

        return uploadResult["secure_url"] as String

    }

    private fun compressImage(file: MultipartFile): ByteArray {
        try {
            val originalImage = javax.imageio.ImageIO.read(file.inputStream) 
                ?: throw IllegalArgumentException("Failed to read image file")
            
            val targetWidth = 1024
            val targetHeight = (originalImage.height * targetWidth.toDouble() / originalImage.width).toInt()
            
            // If already smaller than target, just attempt compression with original dims
            val finalWidth = if (originalImage.width > targetWidth) targetWidth else originalImage.width
            val finalHeight = if (originalImage.width > targetWidth) targetHeight else originalImage.height

            val resizedImage = java.awt.image.BufferedImage(finalWidth, finalHeight, java.awt.image.BufferedImage.TYPE_INT_RGB)
            val graphics = resizedImage.createGraphics()
            graphics.drawImage(originalImage, 0, 0, finalWidth, finalHeight, null)
            graphics.dispose()

            val outputStream = java.io.ByteArrayOutputStream()
            
            // Get a writer for JPEG
            val writers = javax.imageio.ImageIO.getImageWritersByFormatName("jpg")
            if (!writers.hasNext()) throw IllegalStateException("No JPEG writer found")
            val writer = writers.next()
            
            val ios = javax.imageio.ImageIO.createImageOutputStream(outputStream)
            writer.output = ios
            
            val param = writer.defaultWriteParam
            if (param.canWriteCompressed()) {
                param.compressionMode = javax.imageio.ImageWriteParam.MODE_EXPLICIT
                param.compressionQuality = 0.8f // 80% quality
            }
            
            writer.write(null, javax.imageio.IIOImage(resizedImage, null, null), param)
            
            writer.dispose()
            ios.close()
            
            return outputStream.toByteArray()
        } catch (e: Exception) {
            throw RuntimeException("Failed to compress image", e)
        }
    }

    private fun validateFile(file: MultipartFile) {


        // 2. Validate MIME type
        val contentType = file.contentType ?: throw IllegalArgumentException("Invalid file type")
        if (!contentType.startsWith("image/")) {
            throw IllegalArgumentException("Only image files are allowed")
        }

        // 3. Optional: Validate extension based on content type/filename if strictly required
        // complying with "only image MIME types" rule above should be sufficient for now
    }
}
