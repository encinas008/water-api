package com.dreamsbo.posapi.persistence.entity

import com.dreamsbo.core.annotation.NoArg
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(
    schema = "pos",
    name = "meeting_attendance",
    uniqueConstraints = [UniqueConstraint(columnNames = ["meeting_id", "partner_id", "attendance_date"])]
)
@NoArg
data class MeetingAttendanceEntity(
    @Id
    @Column(name = "meeting_attendance_id")
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meeting_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var meeting: MeetingEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partner_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var partner: PartnerEntity,

    @Column(name = "attendance_date", nullable = false)
    var attendanceDate: LocalDate,

    @Column(name = "present", nullable = false)
    var present: Boolean = false,

    @Column(name = "check_in_time")
    var checkInTime: OffsetDateTime? = null,

    @Column(name = "check_out_time")
    var checkOutTime: OffsetDateTime? = null,

    var createdAt: OffsetDateTime = OffsetDateTime.now(),
    var updatedAt: OffsetDateTime? = null,
    var active: Boolean = true,
)



