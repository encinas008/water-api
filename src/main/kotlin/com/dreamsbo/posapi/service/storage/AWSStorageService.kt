package com.dreamsbo.posapi.service.storage

import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.PutObjectRequest
import com.dreamsbo.posapi.common.ApplicationProperty
import com.dreamsbo.posapi.service.storage.dto.FileUploadOutputDto
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.time.Instant
import java.util.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
@Qualifier("aws-storage")
class AWSStorageService(
    val applicationProperty: ApplicationProperty,
    val amazonS3Client: AmazonS3,
) : Storage {

    override fun uploadFile(file: MultipartFile): FileUploadOutputDto {

        val fileObject: File = convertMultipartFileToFile(file)
        val fileName = "${System.currentTimeMillis()}_${file.originalFilename}"

        amazonS3Client.putObject(
            PutObjectRequest(
                applicationProperty.applicationBucketName,
                fileName,
                fileObject
            )
        )

        fileObject.delete()

        val preSignedLink = generatePreSignedLink(fileName)

        return FileUploadOutputDto(fileName = fileName, imageLink = preSignedLink)
    }

    fun generatePreSignedLink(fileName: String?): String {

        val generatePreSignedUrlRequest: GeneratePresignedUrlRequest = GeneratePresignedUrlRequest(
            applicationProperty.applicationBucketName,
            fileName
        ).withMethod(HttpMethod.GET).withExpiration(
            Date(
                Instant.now()
                    .plusSeconds(applicationProperty.cloudAwsFileExpirationInSeconds)
                    .toEpochMilli()
            )
        )

        val url: URL = amazonS3Client.generatePresignedUrl(generatePreSignedUrlRequest)

        println("FileName = $fileName Pre-Signed URL = $url")

        return url.toString()
    }

    private fun convertMultipartFileToFile(file: MultipartFile): File {

        val convertedFile = File(file.originalFilename)

        try {

            val fileOutputStream = FileOutputStream(convertedFile)

            fileOutputStream.write(file.bytes)
        } catch (e: IOException) {

            print("UNEXPECTED ERROR CONVERTING FILE ${e.message}")
        }

        return convertedFile
    }
}
