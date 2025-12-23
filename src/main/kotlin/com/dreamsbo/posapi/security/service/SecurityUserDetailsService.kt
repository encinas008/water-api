package com.dreamsbo.posapi.security.service

import com.dreamsbo.posapi.persistence.entity.UserEntity
import com.dreamsbo.posapi.persistence.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class SecurityUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {

        val userEntity: UserEntity = userRepository.findByUsername(username)
            .orElseThrow { UsernameNotFoundException("User Not found. Username = $username") }

        return User(userEntity.username, userEntity.password, listOf(SimpleGrantedAuthority("USER")))
    }
}
