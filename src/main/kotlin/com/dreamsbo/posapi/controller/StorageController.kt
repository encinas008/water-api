package com.dreamsbo.posapi.controller

import com.dreamsbo.posapi.service.storage.Storage
import com.dreamsbo.posapi.service.storage.dto.CloudImageOutputDto
import com.dreamsbo.posapi.service.storage.dto.FileUploadOutputDto
import com.dreamsbo.posapi.service.storage.dto.ImageOutputDto
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class StorageController(
    @Qualifier("aws-storage") val awsStorageService: Storage,
    @Qualifier("local-storage") val localStorageService: Storage,
) {

    @PostMapping("/upload/aws")
    fun storeFileOnAWS(@RequestParam(value = "file") multipartFile: MultipartFile): CloudImageOutputDto {

        val fileUploadOutputDto: FileUploadOutputDto = awsStorageService.uploadFile(multipartFile)

        return CloudImageOutputDto(
            fileName = fileUploadOutputDto.fileName,
            contentType = multipartFile.contentType,
            contentSize = multipartFile.size,
            imageLink = fileUploadOutputDto.imageLink
        )
    }

    @PostMapping("/upload")
    fun storeFileOnDisk(@RequestParam(value = "file") multipartFile: MultipartFile): ImageOutputDto {

        val fileUploadOutputDto: FileUploadOutputDto = localStorageService.uploadFile(multipartFile)

        return ImageOutputDto(
            fileName = fileUploadOutputDto.fileName,
            contentType = multipartFile.contentType,
            contentSize = multipartFile.size,
            imageLink = fileUploadOutputDto.imageLink
        )
    }
}
