package dev.pcvolkmer.oncoanalytics.monitor

import dev.pcvolkmer.oncoanalytics.monitor.conditions.ConditionInMemoryRepository
import dev.pcvolkmer.oncoanalytics.monitor.conditions.Statistics
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import reactor.core.publisher.Sinks

typealias StatisticsSink = Sinks.Many<Statistics>

@SpringBootApplication
class OncoAnalyticsMonitorApplication {

    @Bean
    fun statisticsEventProducer(): StatisticsSink {
        return Sinks.many().multicast().directBestEffort()
    }

    @Bean
    fun obdsXmlConditionRepository(): ConditionInMemoryRepository {
        return ConditionInMemoryRepository()
    }

    @Bean
    fun obdsFhirConditionRepository(): ConditionInMemoryRepository {
        return ConditionInMemoryRepository()
    }

    @Bean
    fun fhirPseudonymizedConditionRepository(): ConditionInMemoryRepository {
        return ConditionInMemoryRepository()
    }

}

fun main(args: Array<String>) {
    runApplication<OncoAnalyticsMonitorApplication>(*args)
}