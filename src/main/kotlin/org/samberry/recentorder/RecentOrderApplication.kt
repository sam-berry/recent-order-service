package org.samberry.recentorder

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment

class RecentOrderApplication : Application<RecentOrderConfiguration>() {
    override fun initialize(bootstrap: Bootstrap<RecentOrderConfiguration>) {
        bootstrap.objectMapper.registerKotlinModule()
    }

    override fun run(
        configuration: RecentOrderConfiguration,
        environment: Environment
    ) {
        environment.jersey()
            .register(OrderResource(OrderService(OrderTimeline())))
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            RecentOrderApplication().run(*args)
        }
    }
}
