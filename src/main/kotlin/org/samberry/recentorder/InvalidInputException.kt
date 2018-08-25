package org.samberry.recentorder

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

private val formatErrorMessage: (String, Any) -> String = { propertyName: String, invalidValue: Any ->
    "Invalid value '$invalidValue' for '$propertyName'"
}

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
class InvalidInputException(
    propertyName: String,
    invalidValue: Any?
) : RuntimeException(formatErrorMessage(propertyName, invalidValue ?: "null")) {
}