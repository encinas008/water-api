package com.dreamsbo.posapi.security.filter

import com.dreamsbo.posapi.security.SecurityConstants
import com.dreamsbo.posapi.security.service.ClientAuthorization
import com.dreamsbo.posapi.security.service.JwtService
import com.dreamsbo.posapi.security.service.SecurityUserDetailsService
import io.jsonwebtoken.Claims
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.util.UUID
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtRequestFilter(
    private val securityUserDetailsService: SecurityUserDetailsService,
    private val jwtService: JwtService,
    private val clientAuthorization: ClientAuthorization
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {

        if ("/auth/refresh-token" == request.requestURI) {

            filterChain.doFilter(request, response)

            return;
        }

        val authorizationHeader = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER)

        var username = ""
        var jwt = ""

        if (authorizationHeader != null && authorizationHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
            jwt = authorizationHeader.substring(7)
            username = jwtService.extractUsername(jwt)
        }

        if (username.isNotEmpty() && SecurityContextHolder.getContext().authentication == null) {

            val userDetails = securityUserDetailsService.loadUserByUsername(username)

            if (jwtService.validateToken(jwt, userDetails)) run {

                val authentication =
                    UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authentication
            }

            val claims: Claims = jwtService.extractAllClaims(jwt)
            val userId = claims.get(SecurityConstants.USER_ID_FIELD, String::class.java)

            clientAuthorization.userId = UUID.fromString(userId)
            clientAuthorization.username = username
        }

        filterChain.doFilter(request, response)
    }
}
