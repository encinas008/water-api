package com.dreamsbo.posapi.common.aws

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.dreamsbo.posapi.common.ApplicationProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class StorageConfiguration(val applicationProperty: ApplicationProperty) {

    @Bean
    fun amazonS3Client(): AmazonS3 {

        val credentials: AWSCredentials = BasicAWSCredentials(
            applicationProperty.cloudAwsCredentialsAccessKey,
            applicationProperty.cloudAwsCredentialsAccessSecret
        )

        return AmazonS3ClientBuilder
            .standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.US_EAST_2)
            .build()
    }
}
