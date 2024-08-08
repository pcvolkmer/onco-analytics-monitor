package dev.pcvolkmer.oncoanalytics.monitor.topiclisteners

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import dev.pcvolkmer.oncoanalytics.monitor.StatisticsSink
import dev.pcvolkmer.oncoanalytics.monitor.conditions.Condition
import dev.pcvolkmer.oncoanalytics.monitor.conditions.ConditionId
import dev.pcvolkmer.oncoanalytics.monitor.conditions.ConditionRepository
import dev.pcvolkmer.oncoanalytics.monitor.fetchStatistics
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.xpath.XPathFactory

/**
 * oBDS TopicMonitor to listen to Kafka Topics matching 'onkostar.MELDUNG_EXPORT.*'
 *
 * @property statisticsEventProducer The event producer/sink to notify about saved condition
 *
 * @author Paul-Christian Volkmer
 * @since 0.1.0
 */
@Component
class ObdsXmlTopicMonitor(
    @Qualifier("obdsXmlConditionRepository")
    private val conditionRepository: ConditionRepository,
    private val objectMapper: ObjectMapper,
    statisticsEventProducer: StatisticsSink,
) : AbstractTopicMonitor(statisticsEventProducer) {

    @KafkaListener(topicPattern = "onkostar.MELDUNG_EXPORT.*")
    override fun handleTopicRecord(
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) timestamp: Long,
        @Header(KafkaHeaders.RECEIVED_KEY) key: String,
        @Payload payload: String,
    ) {
        try {
            val p = objectMapper.readValue(payload, Record::class.java)
            val xPath = XPathFactory.newDefaultInstance().newXPath()

            // Use local-name() due to XML namespace
            val patientId = xPath.evaluate(
                "//*[local-name()='Patienten_Stammdaten']/@Patient_ID",
                InputSource(StringReader(p.payload.data))
            )
            val tumorId = xPath.evaluate(
                "//*[local-name()='Tumorzuordnung']/@Tumor_ID",
                InputSource(StringReader(p.payload.data))
            )
            val icd10 = xPath.evaluate(
                "//*[local-name()='Primaertumor_ICD_Code']/text()",
                InputSource(StringReader(p.payload.data))
            )

            val updated = conditionRepository.saveIfNewerVersion(
                Condition(
                    ConditionId.fromPatientIdAndTumorId(patientId, tumorId),
                    icd10,
                    p.payload.version
                )
            )

            if (updated) {
                sendUpdatedStatistics(fetchStatistics("obdsxml", conditionRepository))
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class Record @JsonCreator constructor(val payload: RecordPayload)

    class RecordPayload @JsonCreator constructor(
        @JsonAlias("ID") val id: Int,
        @JsonAlias("YEAR") val year: String,
        @JsonAlias("VERSIONSNUMMER") val version: Int,
        @JsonAlias("XML_DATEN") val data: String
    )
}