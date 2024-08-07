package dev.pcvolkmer.oncoanalytics.monitor.topiclisteners

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import dev.pcvolkmer.oncoanalytics.monitor.StatisticsSink
import dev.pcvolkmer.oncoanalytics.monitor.conditions.ConditionRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.core.io.ClassPathResource
import reactor.core.publisher.Sinks
import reactor.test.StepVerifier
import java.time.Instant
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@ExtendWith(MockitoExtension::class)
class ObdsXmlTopicMonitorTest {

    private lateinit var topicMonitor: ObdsXmlTopicMonitor

    private lateinit var statisticsEventProducer: StatisticsSink

    private val objectMapper = JsonMapper.builder()
        .addModules(ParameterNamesModule())
        .enable(MapperFeature.AUTO_DETECT_CREATORS)
        .build()

    private fun payload(): String {
        return ClassPathResource("testobds.json").getContentAsString(Charsets.UTF_8)
    }

    @BeforeEach
    fun setup(
        @Mock conditionRepository: ConditionRepository,
    ) {
        this.statisticsEventProducer = Sinks.many().multicast().directBestEffort()
        this.topicMonitor = ObdsXmlTopicMonitor(conditionRepository, objectMapper, statisticsEventProducer)

        whenever(conditionRepository.saveIfNewerVersion(any())).thenReturn(true)
    }

    @Test
    fun shouldHandleKafkaRecordAndEmitEvent() {
        val stepVerifier = StepVerifier
            .create(statisticsEventProducer.asFlux())
            .expectNextCount(1)
            .expectTimeout(3.seconds.toJavaDuration())
            .verifyLater()

        this.topicMonitor.handleTopicRecord("test", Instant.now().toEpochMilli(), "{\"ID\": 1}", payload())

        stepVerifier.verify()
    }

}