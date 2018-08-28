package org.samberry.recentorder

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Primary
@Component
class ApplicationObjectMapper : ObjectMapper() {
    init {
        registerModule(KotlinModule())
    }
}