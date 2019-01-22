package com.exoscale.circuitbreaker.shop

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.timelimiter.TimeLimiter
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import java.time.Duration

@SpringBootApplication
class ShopApplication {

    @Bean
    fun restTemplate() = RestTemplate()

    @Bean
    fun cache() = mutableMapOf<String, Double?>()

    @Bean
    fun timeLimiter(): TimeLimiter = TimeLimiter.of(Duration.ofSeconds(2))

    @Bean
    fun circuitBreaker() = CircuitBreaker.of("circuit-breaker",
        CircuitBreakerConfig.custom()
            .ringBufferSizeInClosedState(5)
            .waitDurationInOpenState(Duration.ofSeconds(20))
            .build())

    @Bean
    fun service(restTemplate: RestTemplate,
                cache: MutableMap<String, Double?>,
                timeLimiter: TimeLimiter,
                circuitBreaker: CircuitBreaker) = FetchQuoteService(restTemplate, cache, timeLimiter, circuitBreaker)
}

fun main(args: Array<String>) {
    SpringApplication.run(ShopApplication::class.java, *args)
}

@RestController
class FetchQuoteController(private val service: FetchQuoteService) {

    @GetMapping("/product/{id}")
    fun fetch(@PathVariable id: String) = service.getQuote(id)
}