package dev.pcvolkmer.oncoanalytics.monitor.web

import dev.pcvolkmer.oncoanalytics.monitor.StatisticsSink
import dev.pcvolkmer.oncoanalytics.monitor.conditions.Statistics
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import reactor.core.publisher.Sinks
import reactor.test.StepVerifier
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@ExtendWith(SpringExtension::class)
@WebFluxTest
class EventStreamControllerTest {

    private lateinit var webClient: WebTestClient
    private lateinit var statisticsEventProducer: StatisticsSink

    @BeforeEach
    fun setup() {
        this.statisticsEventProducer = Sinks.many().multicast().onBackpressureBuffer(10)
        val controller = EventStreamController(this.statisticsEventProducer)
        this.webClient = WebTestClient.bindToController(controller).build()
    }

    @Test
    fun shouldSendEvents() {
        this.statisticsEventProducer.emitNext(Statistics("test", emptyList())) { _, _ -> false }

        val result = webClient
            .get().uri("/events")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk
            .returnResult<Statistics>()

        StepVerifier.create(result.responseBody)
            .expectNextCount(1)
            .expectTimeout(3.seconds.toJavaDuration())
            .verify()
    }

}