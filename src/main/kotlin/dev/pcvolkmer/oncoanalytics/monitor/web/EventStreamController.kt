package dev.pcvolkmer.oncoanalytics.monitor.web

import dev.pcvolkmer.oncoanalytics.monitor.StatisticsSink
import dev.pcvolkmer.oncoanalytics.monitor.conditions.Statistics
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration

@RestController
class EventStreamController(
    private val statisticsEventProducer: StatisticsSink,
) {

    @GetMapping(path = ["/events"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun eventStream(): Flux<ServerSentEvent<Statistics>> {
        return statisticsEventProducer.asFlux()
            .window(500.milliseconds.toJavaDuration())
            .flatMap { statistics -> statistics.groupBy { it.name } }
            .flatMap { group -> group.last() }
            .map {
                ServerSentEvent.builder(it).event(it.name).build()
            }
    }

}