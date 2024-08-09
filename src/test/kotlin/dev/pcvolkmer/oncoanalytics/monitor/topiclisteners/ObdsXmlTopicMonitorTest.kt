package dev.pcvolkmer.oncoanalytics.monitor.topiclisteners

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import dev.pcvolkmer.oncoanalytics.monitor.StatisticsSink
import dev.pcvolkmer.oncoanalytics.monitor.conditions.Condition
import dev.pcvolkmer.oncoanalytics.monitor.conditions.ConditionId
import dev.pcvolkmer.oncoanalytics.monitor.conditions.ConditionRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.core.io.ClassPathResource
import reactor.core.publisher.Sinks
import reactor.test.StepVerifier
import java.time.Instant
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@ExtendWith(MockitoExtension::class)
class ObdsXmlTopicMonitorTest {

    private lateinit var conditionRepository: ConditionRepository

    private lateinit var topicMonitor: ObdsXmlTopicMonitor

    private lateinit var statisticsEventProducer: StatisticsSink

    private val objectMapper = JsonMapper.builder()
        .addModules(ParameterNamesModule())
        .enable(MapperFeature.AUTO_DETECT_CREATORS)
        .build()

    private fun obds2Payload(): String {
        return ClassPathResource("testobds.json").getContentAsString(Charsets.UTF_8)
    }

    private fun obds3Payload(): String {
        return ClassPathResource("testobds3.json").getContentAsString(Charsets.UTF_8)
    }

    @BeforeEach
    fun setup(
        @Mock conditionRepository: ConditionRepository,
    ) {
        this.conditionRepository = conditionRepository
        this.statisticsEventProducer = Sinks.many().multicast().directBestEffort()
        this.topicMonitor = ObdsXmlTopicMonitor(conditionRepository, objectMapper, statisticsEventProducer)

        whenever(conditionRepository.saveIfNewerVersion(any())).thenReturn(true)
    }

    @Test
    fun shouldHandleObds2KafkaRecordAndEmitEvent() {
        val stepVerifier = StepVerifier
            .create(statisticsEventProducer.asFlux())
            .expectNextCount(1)
            .expectTimeout(3.seconds.toJavaDuration())
            .verifyLater()

        this.topicMonitor.handleTopicRecord("test", Instant.now().toEpochMilli(), "{\"ID\": 1}", obds2Payload())

        stepVerifier.verify()
    }

    @Test
    fun shouldHandleObds3KafkaRecordAndEmitEvent() {
        val stepVerifier = StepVerifier
            .create(statisticsEventProducer.asFlux())
            .expectNextCount(1)
            .expectTimeout(3.seconds.toJavaDuration())
            .verifyLater()

        this.topicMonitor.handleTopicRecord("test", Instant.now().toEpochMilli(), "{\"ID\": 1}", obds3Payload())

        stepVerifier.verify()
    }

    @Test
    fun shouldSaveConditionOnObds2Record() {
        this.topicMonitor.handleTopicRecord("test", Instant.now().toEpochMilli(), "{\"ID\": 1}", obds2Payload())

        val captor = argumentCaptor<Condition>()
        verify(conditionRepository, times(1)).saveIfNewerVersion(captor.capture())
        assertThat(captor.firstValue).isEqualTo(
            Condition(
                ConditionId.fromPatientIdAndTumorId("00001234", "1"),
                "C17.1",
                1
            )
        )
    }

    @Test
    fun shouldSaveConditionOnObds3Record() {
        this.topicMonitor.handleTopicRecord("test", Instant.now().toEpochMilli(), "{\"ID\": 1}", obds3Payload())

        val captor = argumentCaptor<Condition>()
        verify(conditionRepository, times(1)).saveIfNewerVersion(captor.capture())
        assertThat(captor.firstValue).isEqualTo(
            Condition(
                ConditionId.fromPatientIdAndTumorId("00001234", "1"),
                "C17.1",
                1
            )
        )
    }

}