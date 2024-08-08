package dev.pcvolkmer.oncoanalytics.monitor.topiclisteners

import ca.uhn.fhir.context.FhirContext
import dev.pcvolkmer.oncoanalytics.monitor.conditions.Condition
import dev.pcvolkmer.oncoanalytics.monitor.conditions.ConditionId
import dev.pcvolkmer.oncoanalytics.monitor.conditions.Statistics
import org.hl7.fhir.r4.model.Bundle
import reactor.core.publisher.Sinks

/**
 * Abstract class with common methods for FHIR TopicMonitors
 *
 * @property statisticsEventProducer The event producer/sink to notify about saved condition
 *
 * @author Paul-Christian Volkmer
 * @since 0.1.0
 */
abstract class AbstractFhirTopicMonitor(statisticsEventProducer: Sinks.Many<Statistics>) :
    AbstractTopicMonitor(statisticsEventProducer) {

    private val fhirContext = FhirContext.forR4()

    /**
     * Handle parsable FHIR resource
     *
     * @param payload The string representation of the FHIR resource
     * @param handler The handler function to define what to do if payload contains usable FHIR condition
     */
    fun handleUsableFhirPayload(payload: String, handler: (condition: Condition) -> Unit) {
        val bundle = fhirContext.newJsonParser().parseResource(Bundle::class.java, payload)
        val firstEntry = bundle.entry.firstOrNull() ?: return

        if (firstEntry.resource.fhirType() == "Condition") {
            val condition = firstEntry.resource as org.hl7.fhir.r4.model.Condition
            handler(
                Condition(
                    ConditionId(condition.id),
                    condition.code.coding.first { "http://fhir.de/CodeSystem/bfarm/icd-10-gm" == it.system }.code
                )
            )
        }
    }

}