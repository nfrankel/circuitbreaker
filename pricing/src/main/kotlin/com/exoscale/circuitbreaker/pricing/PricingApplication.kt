package com.exoscale.circuitbreaker.pricing

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.support.beans
import org.springframework.web.reactive.function.server.*

fun routes(handler: PricingHandler) = router {
    "/quote".nest {
        GET("/{id}", handler::quote)
    }
}

fun beans() = beans {
    bean<PricingHandler>()
    bean {
        routes(ref())
    }
}

@SpringBootApplication
class PricingApplication

fun main(args: Array<String>) {
    runApplication<PricingApplication>(*args) {
        addInitializers(beans())
    }
}