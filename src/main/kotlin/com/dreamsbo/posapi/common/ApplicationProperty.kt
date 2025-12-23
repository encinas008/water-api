package com.dreamsbo.posapi.common

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ApplicationProperty {

    @Value("\${cloud.aws.credentials.access-key}")
    val  cloudAwsCredentialsAccessKey: String = ""

    @Value("\${cloud.aws.credentials.secret-key}")
    val  cloudAwsCredentialsAccessSecret: String = ""

    @Value("\${application.bucket.name}")
    val applicationBucketName: String = ""

    @Value("\${cloud.aws.file.expiration-in-seconds}")
    val cloudAwsFileExpirationInSeconds: Long = 0

    @Value("\${images.client-application.path}")
    val imagesClientApplicationPath: String = ""

    @Value("\${images.store.path}")
    val imagesStorePath: String = ""

    @Value("\${images.access.path}")
    val imagesAccessPath: String = ""

    @Value("\${cloud.aws.enabled}")
    val isCloudAwsEnabled: Boolean = false

    @Value("\${expire.medical-histories.hours}")
    val expireMedicalHistoriesHours: Long = 0

    @Value("\${print.medical-prescription.orientation}")
    val printMedicalPrescriptionOrientation: String = ""
}
