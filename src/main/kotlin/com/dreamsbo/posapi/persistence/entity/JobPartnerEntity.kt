package com.dreamsbo.posapi.persistence.entity

import com.dreamsbo.core.annotation.NoArg
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(
    schema = "pos",
    name = "job_partner",
    uniqueConstraints = [UniqueConstraint(columnNames = ["job_id", "partner_id"])]
)
@NoArg
data class JobPartnerEntity(
    @Id
    @Column(name = "job_partner_id")
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var job: JobEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partner_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var partner: PartnerEntity,

    var createdAt: OffsetDateTime = OffsetDateTime.now(),
    var active: Boolean = true,
)
