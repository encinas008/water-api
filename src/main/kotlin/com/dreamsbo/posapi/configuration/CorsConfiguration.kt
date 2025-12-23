package com.dreamsbo.posapi.configuration

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class CorsConfiguration : Filter {

    override fun doFilter(
        servletRequest: ServletRequest,
        servletResponse: ServletResponse,
        filterChain: FilterChain,
    ) {

        val response = servletResponse as HttpServletResponse
        val request = servletRequest as HttpServletRequest

        response.setHeader("content-type", "application/json")
        response.setHeader("Access-Control-Allow-Origin", "*")
        response.setHeader("Access-Control-Allow-Credentials", "true")
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT, PATCH")
        response.setHeader(
            "Access-Control-Allow-Headers",
            "Access-Control-Allow-Headers, Authorization, content-type, Access-Control-Allow-Origin, Access-Control-Allow-Credentials, Access-Control-Allow-Methods"
        )

        if ("OPTIONS".equals(request.method, ignoreCase = true)) {

            response.status = HttpServletResponse.SC_OK
        } else {

            filterChain.doFilter(servletRequest, servletResponse)
        }
    }
}
