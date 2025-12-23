package com.dreamsbo.posapi.persistence.entity

import com.dreamsbo.core.annotation.NoArg
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate
import java.util.UUID
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.OffsetDateTime

@Entity
@Table(schema = "pos", name = "profile")
@NoArg
data class ProfileEntity(
    @Id
    @Column(name = "profile_id")
    var id: UUID = UUID.randomUUID(),

    var dni: String = "",
    var name: String = "",
    var lastname: String = "",
    var email: String?,
    var cellphone: String?,
    var telephone: String?,
    @Column(name = "cellphone_references")
    var cellphoneReferences: String?,
    var address: String?,
    var birthDate: LocalDate?,
    var occupation: String? = "",

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var country: CountryEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "city_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var city: CityEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gender_type_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var gender: GenderTypeEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "civil_status_type_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var civilStatus: CivilStatusTypeEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    var image: ImageEntity?,

    var createdAt: OffsetDateTime = OffsetDateTime.now(),
    var updatedAt: OffsetDateTime? = null,
    var active: Boolean = true,
)
