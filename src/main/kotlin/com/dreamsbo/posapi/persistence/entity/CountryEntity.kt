package com.dreamsbo.posapi.persistence.entity

import com.dreamsbo.core.annotation.NoArg
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(schema = "pos", name = "country")
@NoArg
data class CountryEntity(
    @Id
    @Column(name = "country_id")
    var id: UUID = UUID.randomUUID(),

    var code: String = "",
    var name: String = "",
    var description: String = "",

    @OneToMany(
        fetch = FetchType.LAZY,
        mappedBy = "country",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    val cities: MutableList<CityEntity> = mutableListOf(),

    var createdAt: OffsetDateTime = OffsetDateTime.now(),
    var updatedAt: OffsetDateTime? = null,
    var active: Boolean = true,
)
