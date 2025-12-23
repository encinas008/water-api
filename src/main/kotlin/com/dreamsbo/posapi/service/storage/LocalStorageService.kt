package com.dreamsbo.posapi.service.storage

import com.dreamsbo.posapi.common.ApplicationProperty
import com.dreamsbo.posapi.common.errorhandler.FileStorageException
import com.dreamsbo.posapi.service.storage.dto.FileUploadOutputDto
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.*


@Service
@Qualifier("local-storage")
class LocalStorageService(val applicationProperty: ApplicationProperty) : Storage {

    override fun uploadFile(file: MultipartFile): FileUploadOutputDto {

        val fileName = StringUtils.cleanPath(file.originalFilename!!)

        if (fileName.contains("..")) {

            throw FileStorageException("Sorry! Filename contains invalid path sequence $fileName")
        }

        val newNameFile = UUID.randomUUID().toString() + fileName

        val storePath: Path = Path.of(
            applicationProperty.imagesClientApplicationPath,
            applicationProperty.imagesStorePath,
            newNameFile
        )

        try {

            Files.copy(file.inputStream, storePath, StandardCopyOption.REPLACE_EXISTING)
        } catch (e: IOException) {

            println(e.message)

            println(e.stackTrace)

            throw FileStorageException("Could not store file $newNameFile. Please try again!")
        }

        val accessPath: Path = Path.of(applicationProperty.imagesAccessPath, newNameFile)

        return FileUploadOutputDto(newNameFile, accessPath.toUri().path)
    }
}
