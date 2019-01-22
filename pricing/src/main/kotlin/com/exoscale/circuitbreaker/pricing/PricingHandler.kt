package com.exoscale.circuitbreaker.pricing

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Mono
import java.lang.Thread.sleep

class PricingHandler {

    @Value("\${app.timeout.duration}")
    private var timeoutDuration: Long = 6000

    @Value("\${app.timeout.graceCount}")
    private var timeoutGraceCount: Long = 5


    private var ticker = 0

    fun quote(req: ServerRequest) = ServerResponse.ok()
        .body(Mono.just(computeQuote(req.pathVariable("id"))))
        .apply { simulateNetwork() }

    private fun computeQuote(id: String): Double {
        val basePrice = id.hashCode().toByte().toDouble()
        val positivePrice = Math.abs(basePrice)
        val jitter = Math.random() / 4 + 1
        val adjustedPrice = positivePrice * jitter
        val finalPrice = Math.ceil(adjustedPrice)
        println("Price for product $id is $finalPrice")
        return finalPrice
    }

    private fun simulateNetwork() {
        ticker += 1
        if (ticker % 5 != 0 && ticker > timeoutGraceCount) {
            sleep(timeoutDuration)
        }
    }
}