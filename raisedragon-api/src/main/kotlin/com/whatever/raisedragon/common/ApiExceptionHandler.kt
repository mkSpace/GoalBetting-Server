package com.whatever.raisedragon.common

import com.whatever.raisedragon.common.exception.BaseException
import com.whatever.raisedragon.common.exception.ExceptionCode.*
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class ApiExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    private fun handlerMethodArgumentNotValidException(
        exception: MethodArgumentNotValidException
    ): Response<Any> {
        return Response.fail(
            exceptionCode = E400_BAD_REQUEST,
            detailMessage = exception.fieldErrors[0].defaultMessage
        )
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException::class)
    private fun handlerConstraintViolationException(
        exception: ConstraintViolationException,
    ): Response<Any> {
        return Response.fail(
            exceptionCode = E400_BAD_REQUEST,
            detailMessage = DETAIL_MESSAGE_BY_PARAMETER_EXCEPTION
        )
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    private fun handleMethodArgumentTypeMismatchException(
        exception: MethodArgumentTypeMismatchException,
    ): Response<Any> {
        return Response.fail(
            exceptionCode = E400_BAD_REQUEST,
            detailMessage = DETAIL_MESSAGE_BY_PARAMETER_EXCEPTION
        )
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException::class)
    private fun handleMissingServletRequestParameterException(
        exception: MissingServletRequestParameterException,
    ): Response<Any> {
        return Response.fail(
            exceptionCode = E400_BAD_REQUEST,
            detailMessage = DETAIL_MESSAGE_BY_PARAMETER_EXCEPTION
        )
    }

    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    private fun httpRequestMethodNotSupportedException(
        exception: HttpRequestMethodNotSupportedException
    ): Response<Any> {
        return Response.fail(
            exceptionCode = E405_METHOD_NOT_ALLOWED,
            detailMessage = DETAIL_MESSAGE_BY_PARAMETER_EXCEPTION
        )
    }

    @ExceptionHandler(BaseException::class)
    private fun handleBaseException(exception: BaseException): ResponseEntity<Response<Unit>> {
        return ResponseEntity
            .status(exception.exceptionCode.httpStatusCode)
            .body(Response.fail(exception))
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception::class)
    private fun handleInternalServerException(exception: Exception): Response<Any> {
        return Response.fail(
            E500_INTERNAL_SERVER_ERROR,
            if (Constants.profile == "dev") exception.stackTraceToString() else null
        )
    }

    companion object {
        const val DETAIL_MESSAGE_BY_PARAMETER_EXCEPTION = "Please Check Your Request Parameter"
    }
}
