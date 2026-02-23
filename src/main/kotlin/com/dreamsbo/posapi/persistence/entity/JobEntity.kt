package com.dreamsbo.posapi.persistence.entity

import com.dreamsbo.core.annotation.NoArg
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(schema = "pos", name = "job")
@NoArg
data class JobEntity(
    @Id
    @Column(name = "job_id")
    var id: UUID = UUID.randomUUID(),

    @Column(name = "name")
    var name: String,

    @Column(name = "start_date")
    var startDate: LocalDate,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String = "",

    @Column(name = "fine", precision = 19, scale = 2)
    var fine: BigDecimal,

    var createdAt: OffsetDateTime = OffsetDateTime.now(),
    var updatedAt: OffsetDateTime? = null,
    var active: Boolean = true,
    @Column(name = "locked")
    var locked: Boolean = false,
)
