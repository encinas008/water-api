package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.RoleFunctionEntity
import java.util.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoleFunctionRepository : JpaRepository<RoleFunctionEntity, UUID>
