package com.exoscale.circuitbreaker.shop

import com.exoscale.circuitbreaker.shop.Origin.Cache
import com.exoscale.circuitbreaker.shop.Origin.Srv
import com.hazelcast.core.IMap
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.timelimiter.TimeLimiter
import io.vavr.control.Try
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.client.RestTemplate
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

class FetchQuoteService(private val template: RestTemplate,
                        private val cache: IMap<String, Double?>,
                        private val timeLimiter: TimeLimiter,
                        private val circuitBreaker: CircuitBreaker) {

    @Value("\${app.services.pricing.url}")
    private lateinit var quoteUrl: String

    fun getQuote(productId: String): Result {
        val call = {
            template
                .getForObject(quoteUrl, Double::class.java, productId)
                .also { cache[productId] = it }
        }
        val future = Supplier<CompletableFuture<Double>> { CompletableFuture.supplyAsync(call) }
        val timeoutDecorated = TimeLimiter.decorateFutureSupplier(timeLimiter, future)
        val cbDecorated = CircuitBreaker.decorateCallable(circuitBreaker, timeoutDecorated)
        return Try.of(cbDecorated::call).fold(
            { Result(Cache, cache[productId]) },
            { Result(Srv, it) }
        )
    }
}

class Result(val origin: Origin, val price: Double?)

enum class Origin {
    Cache, Srv
}