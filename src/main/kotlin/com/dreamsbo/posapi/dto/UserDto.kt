package com.dreamsbo.posapi.dto

import java.time.LocalDate
import java.util.*

data class ProfileInputDto(
    val dni: String,
    val name: String,
    val lastname: String,
    val email: String,
    val cellphone: String,
    val telephone: String,
    val cellphoneReferences: String,
    val address: String?,
    val birthDate: LocalDate?,
    val countryId: UUID,
    val cityId: UUID,
    val genderTypeId: UUID,
    val civilStatusTypeId: UUID,
    val imageId: UUID?,
    val occupation: String?,
)

data class ProfileOutputDto(
    @JvmField val dni: String,
    @JvmField val name: String,
    @JvmField val lastname: String,
    val email: String?,
    @JvmField val cellphone: String?,
    val telephone: String?,
    val cellphoneReferences: String?,
    @JvmField val address: String?,
    val birthDate: LocalDate?,
    @JvmField val country: String,
    @JvmField val city: String,
    val gender: String,
    val civilStatus: String,
    val photoUrl: String?,
    val occupation: String?,
)

data class UserInputDto(
    val username: String,
    val password: String,
    val role: String,
    val profile: ProfileInputDto,
)

data class UpdateUserInputDto(
    val username: String,
    val password: String,
    val profile: ProfileInputDto,
    val role: String?,
    val checkUniqueFields: UniqueFieldsDto
)

data class UserOutputDto(val username: String, val profileId: UUID, val roleId: UUID)

data class UpdateUserOutputDto(val profileId: UUID)

data class UserDetailsOutputDto(val id: UUID, val username: String, val profile: ProfileOutputDto, val role: RoleOutputDto?)

data class UpdateUserStatusInputDto(val active: Boolean)
