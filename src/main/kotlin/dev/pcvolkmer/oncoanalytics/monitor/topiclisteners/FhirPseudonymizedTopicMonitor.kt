package dev.pcvolkmer.oncoanalytics.monitor.topiclisteners

import ca.uhn.fhir.context.FhirContext
import dev.pcvolkmer.oncoanalytics.monitor.StatisticsSink
import dev.pcvolkmer.oncoanalytics.monitor.conditions.Condition
import dev.pcvolkmer.oncoanalytics.monitor.conditions.ConditionId
import dev.pcvolkmer.oncoanalytics.monitor.conditions.ConditionInMemoryRepository
import dev.pcvolkmer.oncoanalytics.monitor.fetchStatistics
import org.hl7.fhir.r4.model.Bundle
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class FhirPseudonymizedTopicMonitor(
    @Qualifier("fhirPseudonymizedConditionRepository")
    private val conditionRepository: ConditionInMemoryRepository,
    statisticsEventProducer: StatisticsSink,
) : TopicMonitor(statisticsEventProducer) {

    @KafkaListener(topicPattern = "fhir.pseudonymized.*")
    override fun handleTopicRecord(
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) timestamp: Long,
        @Header(KafkaHeaders.RECEIVED_KEY) key: String,
        @Payload payload: String,
    ) {
        try {
            val ctx = FhirContext.forR4()
            val parser = ctx.newJsonParser()

            val bundle = parser.parseResource(Bundle::class.java, payload)
            val firstEntry = bundle.entry.firstOrNull() ?: return

            if (firstEntry.resource.fhirType() == "Condition") {
                val condition = firstEntry.resource as org.hl7.fhir.r4.model.Condition
                val updated = conditionRepository.save(
                    Condition(
                        ConditionId(condition.id),
                        condition.code.coding.first { "http://fhir.de/CodeSystem/bfarm/icd-10-gm" == it.system }.code
                    )
                )

                if (updated) {
                    sendUpdatedStatistics(fetchStatistics("fhirpseudonymized", conditionRepository))
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
    }
}