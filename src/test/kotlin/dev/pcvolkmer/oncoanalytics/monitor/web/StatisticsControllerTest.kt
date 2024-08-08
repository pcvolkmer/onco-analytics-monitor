package dev.pcvolkmer.oncoanalytics.monitor.web

import dev.pcvolkmer.oncoanalytics.monitor.allKeys
import dev.pcvolkmer.oncoanalytics.monitor.conditions.Condition
import dev.pcvolkmer.oncoanalytics.monitor.conditions.ConditionId
import dev.pcvolkmer.oncoanalytics.monitor.conditions.ConditionRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.stream.Stream

@ExtendWith(MockitoExtension::class, SpringExtension::class)
@WebFluxTest
@MockitoSettings(strictness = Strictness.LENIENT)
class StatisticsControllerTest {

    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setup(
        @Mock(name = "obdsXmlConditionRepository") obdsXmlConditionRepository: ConditionRepository,
        @Mock(name = "fhirObdsConditionRepository") fhirObdsConditionRepository: ConditionRepository,
        @Mock(name = "fhirPseudonymizedConditionRepository") fhirPseudonymizedConditionRepository: ConditionRepository,
    ) {
        val controller = StatisticsController(
            obdsXmlConditionRepository,
            fhirObdsConditionRepository,
            fhirPseudonymizedConditionRepository
        )
        this.webClient = WebTestClient.bindToController(controller).build()

        whenever(obdsXmlConditionRepository.findAll())
            .thenReturn(listOf(Condition(ConditionId("TESTOBDSXML"), "C00.9", 1)))
        whenever(fhirObdsConditionRepository.findAll())
            .thenReturn(listOf(Condition(ConditionId("TESTFHIROBDS"), "C43.9")))
        whenever(fhirPseudonymizedConditionRepository.findAll())
            .thenReturn(listOf(Condition(ConditionId("TESTFHIRPSEUDO"), "F79.9")))
    }

    @ParameterizedTest(name = "Test request to /statistics/{0}")
    @MethodSource("testDataSource")
    fun shouldGetObdsXmlStatistics(statisticsName: String, entryName: String, entryIndex: Int) {
        webClient
            .get().uri("/statistics/{0}", statisticsName)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectAll(
                { spec -> spec.expectStatus().isOk },
                { spec -> spec.expectHeader().contentType(MediaType.APPLICATION_JSON) },
                { spec -> spec.expectBody().jsonPath("$.name").isEqualTo(statisticsName) },
                { spec -> spec.expectBody().jsonPath("$.entries[%d].name", entryIndex).isEqualTo(entryName) },
                { spec -> spec.expectBody().jsonPath("$.entries[%d].count", entryIndex).isEqualTo(1) }
            )
    }

    companion object {
        @JvmStatic
        fun testDataSource(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("obdsxml", "C00-C14", allKeys.indexOf("C00-C14")),
                Arguments.of("fhirobds", "C43", allKeys.indexOf("C43")),
                Arguments.of("fhirpseudonymized", "Other", allKeys.indexOf("Other"))
            )
        }
    }

}