package com.dreamsbo.posapi.service.storage

import com.dreamsbo.posapi.service.storage.dto.FileUploadOutputDto
import org.springframework.web.multipart.MultipartFile

interface Storage {

    fun uploadFile(file: MultipartFile): FileUploadOutputDto
}
