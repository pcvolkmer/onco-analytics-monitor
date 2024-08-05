package dev.pcvolkmer.oncoanalytics.monitor.topiclisteners

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import dev.pcvolkmer.oncoanalytics.monitor.StatisticsSink
import dev.pcvolkmer.oncoanalytics.monitor.conditions.Condition
import dev.pcvolkmer.oncoanalytics.monitor.conditions.ConditionInMemoryRepository
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

@Component
class ObdsXmlTopicMonitor(
    @Qualifier("obdsXmlConditionRepository")
    private val conditionRepository: ConditionInMemoryRepository,
    private val objectMapper: ObjectMapper,
    statisticsEventProducer: StatisticsSink,
) : TopicMonitor(statisticsEventProducer) {

    @KafkaListener(topicPattern = "input.*")
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
                    Condition.generateConditionId(patientId, tumorId),
                    p.payload.version,
                    icd10
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