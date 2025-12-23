package com.dreamsbo.posapi.security.configuration

import com.dreamsbo.posapi.security.exception.JwtAuthenticationEntryPoint
import com.dreamsbo.posapi.security.filter.JwtRequestFilter
import com.dreamsbo.posapi.security.service.ClientAuthorization
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.context.WebApplicationContext

@Configuration
class WebSecurityConfiguration(
    val jwtRequestFilter: JwtRequestFilter,
    val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
) {

    @Bean
    fun securityFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain {

        httpSecurity.formLogin { it.disable() }
        httpSecurity.csrf { it.disable() }
        httpSecurity.cors(Customizer.withDefaults())

        httpSecurity.authorizeHttpRequests {
            it.requestMatchers("/auth/**").permitAll()
                .anyRequest().authenticated()
        }

        httpSecurity.addFilterBefore(
            jwtRequestFilter,
            UsernamePasswordAuthenticationFilter::class.java
        )

        httpSecurity.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

        httpSecurity.exceptionHandling { it.authenticationEntryPoint(jwtAuthenticationEntryPoint) }

        return httpSecurity.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {

        return BCryptPasswordEncoder()
    }

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager {

        return authenticationConfiguration.authenticationManager
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    fun clientAuthorization(): ClientAuthorization {

        return ClientAuthorization()
    }

//    @Bean
//    fun corsConfigurationSource(): CorsConfigurationSource {
//        val configuration = CorsConfiguration()
//        configuration.allowedOrigins = listOf("http://localhost:4200")
//        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE")
//        configuration.allowedHeaders = listOf("authorization", "content-type")
//        val source = UrlBasedCorsConfigurationSource()
//        source.registerCorsConfiguration("/**", configuration)
//        return source
//    }
}
