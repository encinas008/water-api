package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.ImageEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ImageRepository : JpaRepository<ImageEntity, UUID>
