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
    name = "meeting_partner",
    uniqueConstraints = [UniqueConstraint(columnNames = ["meeting_id", "partner_id"])]
)
@NoArg
data class MeetingPartnerEntity(
    @Id
    @Column(name = "meeting_partner_id")
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meeting_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var meeting: MeetingEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partner_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var partner: PartnerEntity,

    var createdAt: OffsetDateTime = OffsetDateTime.now(),
    var active: Boolean = true,
)

