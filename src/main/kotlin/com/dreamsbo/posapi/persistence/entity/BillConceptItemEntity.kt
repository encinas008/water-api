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
@Table(schema = "pos", name = "bill_concept_item")
@NoArg
data class BillConceptItemEntity(
    @Id
    @Column(name = "bill_concept_item_id")
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "water_bill_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var waterBill: WaterBillEntity,

    @Column(name = "concept_name")
    var conceptName: String,  // Ej: "Aporte al deporte", "Tarifa básica", "Aporte a la OTB"

    @Column(name = "assigned_date")
    var assignedDate: LocalDate,  // Fecha asignada del concepto

    @Column(name = "amount")
    var amount: BigDecimal,  // Importe del concepto

    @Column(name = "fine_type", length = 20)
    var fineType: String? = null, // "JOB", "MEETING" o null si es un concepto regular

    @Column(name = "fine_id")
    var fineId: UUID? = null, // ID de la asistencia

    var createdAt: OffsetDateTime = OffsetDateTime.now(),
    var updatedAt: OffsetDateTime? = null,
    var active: Boolean = true,
)






