package dev.pcvolkmer.oncoanalytics.monitor.topiclisteners

import dev.pcvolkmer.oncoanalytics.monitor.StatisticsSink
import dev.pcvolkmer.oncoanalytics.monitor.conditions.ConditionRepository
import dev.pcvolkmer.oncoanalytics.monitor.fetchStatistics
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

/**
 * FHIR TopicMonitor to listen to Kafka Topics matching 'fhir.obds.Condition.*'
 *
 * @property statisticsEventProducer The event producer/sink to notify about saved condition
 *
 * @author Paul-Christian Volkmer
 * @since 0.1.0
 */
@Component
class FhirObdsTopicMonitor(
    @Qualifier("fhirObdsConditionRepository")
    private val conditionRepository: ConditionRepository,
    statisticsEventProducer: StatisticsSink,
) : AbstractFhirTopicMonitor(statisticsEventProducer) {

    @KafkaListener(topicPattern = "fhir.obds.Condition.*")
    override fun handleTopicRecord(
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) timestamp: Long,
        @Header(KafkaHeaders.RECEIVED_KEY) key: String,
        @Payload payload: String,
    ) {
        try {
            this.handleUsableFhirPayload(payload) { condition ->
                if (conditionRepository.save(condition)) {
                    sendUpdatedStatistics(fetchStatistics("fhirobds", conditionRepository))
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
    }
}