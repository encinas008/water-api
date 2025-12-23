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
@Table(schema = "pos", name = "water_meter_reading")
@NoArg
data class WaterMeterReadingEntity(
    @Id
    @Column(name = "reading_id")
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partner_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var partner: PartnerEntity,

    @Column(name = "reading_date")
    var readingDate: LocalDate,

    @Column(name = "previous_reading")
    var previousReading: BigDecimal,

    @Column(name = "current_reading")
    var currentReading: BigDecimal,

    @Column(name = "consumption")
    var consumption: BigDecimal,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reader_user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    var readerUser: UserEntity? = null,  // Opcional: puede ser null si no se envía userId

    var observation: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    var image: ImageEntity? = null,

    var createdAt: OffsetDateTime = OffsetDateTime.now(),
    var updatedAt: OffsetDateTime? = null,
    var active: Boolean = true,
)
