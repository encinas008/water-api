package com.dreamsbo.posapi.persistence.entity

import com.dreamsbo.core.annotation.NoArg
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(schema = "pos", name = "meeting")
@NoArg
data class MeetingEntity(
    @Id
    @Column(name = "meeting_id")
    var id: UUID = UUID.randomUUID(),

    @Column(name = "name")
    var name: String,

    @Column(name = "meeting_date")
    var meetingDate: LocalDate,

    @Column(name = "hour")
    var hour: Int, // Hora en formato 12 horas (1-12)

    @Column(name = "minute")
    var minute: Int, // Minuto (0-59)

    @Column(name = "am_pm")
    var amPm: String, // "AM" o "PM"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_type_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    var meetingType: MeetingTypeEntity? = null,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String = "",

    @Column(name = "fine", precision = 19, scale = 2)
    var fine: java.math.BigDecimal? = null,

    var createdAt: OffsetDateTime = OffsetDateTime.now(),
    var updatedAt: OffsetDateTime? = null,
    var active: Boolean = true,
)

