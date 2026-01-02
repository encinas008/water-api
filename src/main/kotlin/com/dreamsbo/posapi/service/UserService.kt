package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.common.errorhandler.BadRequestException
import com.dreamsbo.posapi.common.errorhandler.ConflictException
import com.dreamsbo.posapi.common.errorhandler.NotFoundEntityException
import com.dreamsbo.posapi.common.errorhandler.UnauthorizedException
import com.dreamsbo.posapi.dto.*
import com.dreamsbo.posapi.persistence.entity.ImageEntity
import com.dreamsbo.posapi.persistence.entity.ProfileEntity
import com.dreamsbo.posapi.persistence.entity.UserEntity
import com.dreamsbo.posapi.persistence.entity.UserRoleEntity
import com.dreamsbo.posapi.persistence.entity.BoxEntity
import com.dreamsbo.posapi.persistence.repository.*
import com.dreamsbo.posapi.security.SecurityApplicationProperty
import com.dreamsbo.posapi.security.service.JwtService
import com.dreamsbo.posapi.security.service.SecurityUserDetailsService
import com.dreamsbo.posapi.type.RoleType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.*

@Service
class UserService(
    val authenticationManager: AuthenticationManager,
    val userDetailsService: SecurityUserDetailsService,
    val jwtService: JwtService,
    val userRepository: UserRepository,
    val profileRepository: ProfileRepository,
    val roleRepository: RoleRepository,
    val passwordEncoder: PasswordEncoder,
    val userRoleRepository: UserRoleRepository,
    val countryRepository: CountryRepository,
    val cityRepository: CityRepository,
    val genderTypeRepository: GenderTypeRepository,
    val civilStatusTypeRepository: CivilStatusTypeRepository,
    val imageRepository: ImageRepository,
    val securityApplicationProperty: SecurityApplicationProperty,
    val boxRepository: BoxRepository,
) {

    fun create(userInputDto: UserInputDto): UserOutputDto {

        val country = countryRepository.findById(userInputDto.profile.countryId)
        if (country.isEmpty) {

            throw BadRequestException("El ${userInputDto.profile.dni} Pais no ha sido encontrado")
        }

        val city = cityRepository.findById(userInputDto.profile.cityId)
        if (city.isEmpty) {

            throw BadRequestException("El ${userInputDto.profile.dni} Ciudad no ha sido encontrado")
        }

        val gender = genderTypeRepository.findById(userInputDto.profile.genderTypeId)
        if (gender.isEmpty) {

            throw BadRequestException("El ${userInputDto.profile.dni} Genero no ha sido encontrado")
        }

        val civilStatus =
            civilStatusTypeRepository.findById(userInputDto.profile.civilStatusTypeId)
        if (civilStatus.isEmpty) {

            throw BadRequestException("El ${userInputDto.profile.dni} Estado civil no ha sido encontrado")
        }

        var image: ImageEntity? = null
        if (userInputDto.profile.imageId != null) {
            image = imageRepository.findById(userInputDto.profile.imageId).orElse(null)
        }

        val profileToSave = ProfileEntity(
            dni = userInputDto.profile.dni,
            name = userInputDto.profile.name,
            lastname = userInputDto.profile.lastname,
            email = userInputDto.profile.email,
            cellphone = userInputDto.profile.cellphone,
            telephone = userInputDto.profile.telephone,
            cellphoneReferences = userInputDto.profile.cellphoneReferences,
            address = userInputDto.profile.address,
            birthDate = userInputDto.profile.birthDate,
            country = country.get(),
            city = city.get(),
            gender = gender.get(),
            civilStatus = civilStatus.get(),
            image = image,
        )

        val profileCreated = profileRepository.save(profileToSave)

        val role = roleRepository.findByNameAndActive(userInputDto.role, true)
            .orElseThrow { Exception("Has not been found role ${RoleType.DOCTOR.name}") }

        val passwordEncoded = passwordEncoder.encode(userInputDto.password)

        val userEntity =
            UserEntity(
                username = userInputDto.username,
                password = passwordEncoded,
                profile = profileCreated,
                userRole = null
            )

        val userCreated = userRepository.save(userEntity)

        val userRoleCreated =
            userRoleRepository.save(UserRoleEntity(user = userCreated, role = role))

        if (role.name.uppercase() == "ADMINISTRADOR" || role.name.uppercase() == "CAJERO") {
            boxRepository.save(
                BoxEntity(
                    name = "CAJA PRINCIPAL",
                    description = "Caja creada automáticamente para el usuario ${userCreated.username}",
                    user = userCreated
                )
            )
        }

        return UserOutputDto(
            username = userCreated.username,
            profileId = userCreated.profile.id,
            roleId = userRoleCreated.role.id
        )
    }

    fun signIn(authenticationInputDto: AuthenticationInputDto): AuthenticationOutputDto {

        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                authenticationInputDto.username,
                authenticationInputDto.password
            )
        )

        if (authentication.isAuthenticated) {

            val userDetails = userDetailsService.loadUserByUsername(authenticationInputDto.username)

            val user = userRepository.findByUsername(userDetails.username).get()

            if (!user.active) {

                throw UnauthorizedException("Usuario desactivado")
            }

            return jwtService.generateToken(
                user,
                securityApplicationProperty.securityAuthenticationJwtTokenValidityInSeconds,
                null
            )
        } else {

            throw UsernameNotFoundException("Invalid user request!")
        }
    }

    fun getUserById(userId: UUID): UserDetailsOutputDto {

        return userRepository.findById(userId).map {

            val profile = ProfileOutputDto(
                dni = it.profile.dni,
                name = it.profile.name,
                lastname = it.profile.lastname,
                email = it.profile.email,
                cellphone = it.profile.cellphone,
                telephone = it.profile.telephone,
                cellphoneReferences = it.profile.cellphoneReferences,
                address = it.profile.address,
                birthDate = it.profile.birthDate,
                country = it.profile.country.name,
                city = it.profile.city.name,
                gender = it.profile.gender.name,
                civilStatus = it.profile.civilStatus.name,
                photoUrl = it.profile.image?.url ?: "",
                occupation = it.profile.occupation
            )

            val roleDto = it.userRole?.firstOrNull()?.let { userRole ->
                RoleOutputDto(
                    id = userRole.role.id,
                    name = userRole.role.name,
                    description = userRole.role.description,
                    code = userRole.role.code
                )
            }

            UserDetailsOutputDto(it.id, it.username, profile, roleDto)
        }.orElseThrow {

            NotFoundEntityException("Has not been found user. UserId = $userId")
        }
    }

    fun getAll(): List<UserDetailsOutputDto> {

        return userRepository.findAll().map {

            val profile = ProfileOutputDto(
                dni = it.profile.dni,
                name = it.profile.name,
                lastname = it.profile.lastname,
                email = it.profile.email,
                cellphone = it.profile.cellphone,
                telephone = it.profile.telephone,
                cellphoneReferences = it.profile.cellphoneReferences,
                address = it.profile.address,
                birthDate = it.profile.birthDate,
                country = it.profile.country.name,
                city = it.profile.city.name,
                gender = it.profile.gender.name,
                civilStatus = it.profile.civilStatus.name,
                photoUrl = it.profile.image?.url ?: "",
                occupation = it.profile.occupation
            )

            val roleDto = it.userRole?.firstOrNull()?.let { userRole ->
                RoleOutputDto(
                    id = userRole.role.id,
                    name = userRole.role.name,
                    description = userRole.role.description,
                    code = userRole.role.code
                )
            }

            UserDetailsOutputDto(it.id, it.username, profile, roleDto)
        }
    }

    fun update(userId: UUID, userInputDto: UpdateUserInputDto): UpdateUserOutputDto {

        val country = countryRepository.findById(userInputDto.profile.countryId)
        if (country.isEmpty) {

            throw BadRequestException("El ${userInputDto.profile.dni} Pais no ha sido encontrado")
        }

        val city = cityRepository.findById(userInputDto.profile.cityId)
        if (city.isEmpty) {

            throw BadRequestException("El ${userInputDto.profile.dni} Ciudad no ha sido encontrado")
        }

        val gender = genderTypeRepository.findById(userInputDto.profile.genderTypeId)
        if (gender.isEmpty) {

            throw BadRequestException("El ${userInputDto.profile.dni} Genero no ha sido encontrado")
        }

        val civilStatus =
            civilStatusTypeRepository.findById(userInputDto.profile.civilStatusTypeId)
        if (civilStatus.isEmpty) {

            throw BadRequestException("El ${userInputDto.profile.dni} Estado civil no ha sido encontrado")
        }

        val userToBeUpdated = userRepository.findById(userId).orElseThrow {

            throw NotFoundEntityException("No se ha podido entrar el usuario para actualizar. UserId = ${userId}")
        }

        if (userInputDto.checkUniqueFields.isUsernameUpdated == true) {

            val userOptional = userRepository.findByUsername(username = userInputDto.username)
            if (!userOptional.isEmpty) {

                throw ConflictException("Ya esta siendo utilizado el alias. Alias = ${userInputDto.username}")
            }
        }

        if (userInputDto.checkUniqueFields.isDniUpdated == true) {

            val patient = profileRepository.findByDni(userInputDto.profile.dni)
            if (!patient.isEmpty) {

                throw ConflictException("El CI = ${userInputDto.profile.dni} ya esta siendo utilizado!")
            }
        }

        userToBeUpdated.profile.dni = userInputDto.profile.dni
        userToBeUpdated.profile.name = userInputDto.profile.name
        userToBeUpdated.profile.lastname = userInputDto.profile.lastname
        userToBeUpdated.profile.email = userInputDto.profile.email
        userToBeUpdated.profile.cellphone = userInputDto.profile.cellphone
        userToBeUpdated.profile.telephone = userInputDto.profile.telephone
        userToBeUpdated.profile.cellphoneReferences = userInputDto.profile.cellphoneReferences
        userToBeUpdated.profile.address = userInputDto.profile.address
        userToBeUpdated.profile.birthDate = userInputDto.profile.birthDate
        userToBeUpdated.profile.country = country.get()
        userToBeUpdated.profile.city = city.get()
        userToBeUpdated.profile.gender = gender.get()
        userToBeUpdated.profile.civilStatus = civilStatus.get()
        userToBeUpdated.profile.civilStatus = civilStatus.get()
        userToBeUpdated.username = userInputDto.username

        // Update Role
        if (userInputDto.role != null) {
             val roleEntity = roleRepository.findByNameAndActive(userInputDto.role, true)
                .orElseThrow { NotFoundEntityException("Rol no encontrado: ${userInputDto.role}") }

             val userRoles = userRoleRepository.findByUserId(userId)
             if (userRoles.isNotEmpty()) {
                 val userRole = userRoles[0]
                 userRoleRepository.delete(userRole)
                 if (userRoles.size > 1) {
                     for (i in 1 until userRoles.size) {
                        userRoleRepository.delete(userRoles[i])
                     }
                 }
             }
             userRoleRepository.save(UserRoleEntity(user = userToBeUpdated, role = roleEntity))
        }


        userToBeUpdated.updatedAt = OffsetDateTime.now()

        val updatedProfile = profileRepository.save(userToBeUpdated.profile)

        if (userInputDto.password != "UUID202312") {

            val passwordEncoded = passwordEncoder.encode(userInputDto.password)

            userToBeUpdated.password = passwordEncoded

            println("UPDATED USER FOR PASSWORD")
        }

        userRepository.save(userToBeUpdated)

        return UpdateUserOutputDto(
            profileId = updatedProfile.id
        )
    }

    fun updateStatus(userId: UUID, userStatus: UpdateUserStatusInputDto): UpdateUserOutputDto {

        val user = userRepository.findById(userId).orElseThrow {

            NotFoundEntityException("No se ha encontrado el user. UserId = $userId")
        }

        user.active = false
        user.updatedAt = OffsetDateTime.now()
        user.active = userStatus.active

        val updatedUser = userRepository.save(user)

        return UpdateUserOutputDto(updatedUser.profile.id)
    }
}
