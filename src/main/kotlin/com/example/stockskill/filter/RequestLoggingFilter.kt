package com.example.stockskill.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class RequestLoggingFilter : OncePerRequestFilter() {
    private val log = LoggerFactory.getLogger(RequestLoggingFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val startTime = System.currentTimeMillis()
        val uri = request.requestURI
        val method = request.method
        val queryString = request.queryString?.let { "?$it" } ?: ""

        log.info("Incoming HTTP Request: {} {}{}", method, uri, queryString)

        try {
            filterChain.doFilter(request, response)
        } finally {
            val duration = System.currentTimeMillis() - startTime
            val status = response.status
            log.info("Outgoing HTTP Response: {} {}{} | Status: {} | Duration: {}ms", method, uri, queryString, status, duration)
        }
    }
}
