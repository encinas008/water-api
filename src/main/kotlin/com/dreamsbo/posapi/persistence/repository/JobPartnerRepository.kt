package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.JobPartnerEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface JobPartnerRepository : JpaRepository<JobPartnerEntity, UUID> {

    fun findByJobIdAndActive(jobId: UUID, active: Boolean): List<JobPartnerEntity>

    fun findByPartnerIdAndActive(partnerId: UUID, active: Boolean): List<JobPartnerEntity>

    fun findByJobIdAndPartnerId(jobId: UUID, partnerId: UUID): Optional<JobPartnerEntity>

    @Query("SELECT jp FROM JobPartnerEntity jp WHERE jp.job.id = :jobId AND jp.partner.id = :partnerId AND jp.active = true")
    fun findActiveByJobIdAndPartnerId(
        @Param("jobId") jobId: UUID,
        @Param("partnerId") partnerId: UUID
    ): Optional<JobPartnerEntity>
}
