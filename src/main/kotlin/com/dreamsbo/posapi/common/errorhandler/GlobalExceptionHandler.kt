package com.dreamsbo.posapi.common.errorhandler

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler


class NotFoundEntityException(message: String? = null, cause: Throwable? = null) :
    Exception(message, cause)

class BadRequestException(message: String? = null, cause: Throwable? = null) :
    Exception(message, cause)

class ConflictException(message: String? = null, cause: Throwable? = null) :
    Exception(message, cause)

class QueryPatientConflictException(message: String? = null, cause: Throwable? = null) :
    Exception(message, cause)

class FileStorageException(message: String? = null, cause: Throwable? = null) :
    Exception(message, cause)

class ExpiredTokenException(message: String? = null, cause: Throwable? = null) :
    Exception(message, cause)

class ExpiredRefreshTokenException(message: String? = null, cause: Throwable? = null) :
    Exception(message, cause)

class UnauthorizedException(message: String? = null, cause: Throwable? = null) :
    Exception(message, cause)

@ControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(value = [BadCredentialsException::class])
    fun badCredentialsException(e: Exception): ResponseEntity<Any> {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.message)
    }

    @ExceptionHandler(value = [ExpiredTokenException::class, UnauthorizedException::class])
    fun unauthorizedException(e: ExpiredTokenException): ResponseEntity<Any> {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.message)
    }

    @ExceptionHandler(value = [ExpiredRefreshTokenException::class])
    fun expiredRefreshTokenException(e: ExpiredRefreshTokenException): ResponseEntity<Any> {

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.message)
    }

    @ExceptionHandler(value = [FileStorageException::class])
    fun fileStorageException(): ResponseEntity<Any> {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
    }

    @ExceptionHandler(value = [NotFoundException::class, NotFoundEntityException::class])
    fun notFoundException(e: Exception): ResponseEntity<Any> {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
    }

    @ExceptionHandler(value = [ConflictException::class, DataIntegrityViolationException::class])
    fun conflictException(e: Exception): ResponseEntity<Any> {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.message)
    }

    @ExceptionHandler(value = [QueryPatientConflictException::class])
    fun queryPatientConflictException(e: QueryPatientConflictException): ResponseEntity<Any> {

        val response = QueryPatientConflictOutputDto(id = e.cause?.message, message = e.message)

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }

    @ExceptionHandler(value = [BadRequestException::class])
    fun badRequestException(e: Exception): ResponseEntity<Any> {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
    }

    @ExceptionHandler(value = [Exception::class])
    fun internalException(e: Exception): ResponseEntity<Any> {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
    }
}
