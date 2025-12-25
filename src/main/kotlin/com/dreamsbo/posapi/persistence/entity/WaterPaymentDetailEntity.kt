package com.dreamsbo.posapi.persistence.entity

import com.dreamsbo.core.annotation.NoArg
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(
    schema = "pos",
    name = "water_payment_detail",
    uniqueConstraints = [UniqueConstraint(columnNames = ["water_payment_id", "fine_type", "fine_id"])]
)
@NoArg
data class WaterPaymentDetailEntity(
    @Id
    @Column(name = "water_payment_detail_id")
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "water_payment_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var waterPayment: WaterPaymentEntity,

    @Column(name = "fine_type", nullable = false, length = 20)
    var fineType: String, // "JOB" o "MEETING"

    @Column(name = "fine_id", nullable = false)
    var fineId: UUID, // ID de la ausencia (JobAttendance o MeetingAttendance)

    @Column(name = "fine_name", nullable = false, length = 255)
    var fineName: String, // Nombre del trabajo o reunión

    @Column(name = "fine_date", nullable = false)
    var fineDate: LocalDate, // Fecha de la ausencia

    @Column(name = "fine_amount", nullable = false, precision = 19, scale = 2)
    var fineAmount: BigDecimal, // Monto de la multa

    var createdAt: OffsetDateTime = OffsetDateTime.now(),
    var active: Boolean = true,
)

