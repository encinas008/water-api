package com.dreamsbo.posapi.service.storage.dto

data class CloudImageOutputDto(
    val fileName: String?,
    val contentType: String?,
    val contentSize: Long,
    val imageLink: String,
)

data class ImageOutputDto(
    val fileName: String?,
    val contentType: String?,
    val contentSize: Long,
    val imageLink: String,
)

data class ImageInputDto(
    val fileName: String?,
    val contentType: String?,
    val contentSize: Long,
    val imageLink: String,
)
