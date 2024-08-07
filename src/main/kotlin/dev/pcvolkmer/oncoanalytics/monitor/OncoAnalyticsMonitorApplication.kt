package dev.pcvolkmer.oncoanalytics.monitor

import dev.pcvolkmer.oncoanalytics.monitor.conditions.InMemoryConditionRepository
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
    fun obdsXmlConditionRepository(): InMemoryConditionRepository {
        return InMemoryConditionRepository()
    }

    @Bean
    fun fhirObdsConditionRepository(): InMemoryConditionRepository {
        return InMemoryConditionRepository()
    }

    @Bean
    fun fhirPseudonymizedConditionRepository(): InMemoryConditionRepository {
        return InMemoryConditionRepository()
    }

}

fun main(args: Array<String>) {
    runApplication<OncoAnalyticsMonitorApplication>(*args)
}