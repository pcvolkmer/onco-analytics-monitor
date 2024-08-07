package dev.pcvolkmer.oncoanalytics.monitor.topiclisteners

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
class FhirPseudonymizedTopicMonitorTest {

    private lateinit var topicMonitor: FhirPseudonymizedTopicMonitor

    private lateinit var statisticsEventProducer: StatisticsSink

    private fun payload(): String {
        return ClassPathResource("testfhir.json").getContentAsString(Charsets.UTF_8)
    }

    @BeforeEach
    fun setup(
        @Mock conditionRepository: ConditionRepository,
    ) {
        this.statisticsEventProducer = Sinks.many().multicast().directBestEffort()
        this.topicMonitor = FhirPseudonymizedTopicMonitor(conditionRepository, statisticsEventProducer)

        whenever(conditionRepository.save(any())).thenReturn(true)
    }

    @Test
    fun shouldHandleKafkaRecordAndEmitEvent() {
        val stepVerifier = StepVerifier
            .create(statisticsEventProducer.asFlux())
            .expectNextCount(1)
            .expectTimeout(3.seconds.toJavaDuration())
            .verifyLater()

        this.topicMonitor.handleTopicRecord(
            "test",
            Instant.now().toEpochMilli(),
            "Struct{REFERENZ_NUMMER=00001234,TUMOR_ID=1}",
            payload()
        )

        stepVerifier.verify()
    }

}